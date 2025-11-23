package me.owdding.skyocean.helpers.skilltree

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.lib.extensions.ListMerger
import me.owdding.lib.extensions.applyToTooltip
import me.owdding.lib.extensions.round
import me.owdding.lib.repo.LevelingTreeNode
import me.owdding.lib.repo.TreeRepoData
import me.owdding.lib.repo.WhisperCostType
import me.owdding.skyocean.data.profile.PerkUpgradeStorage
import me.owdding.skyocean.utils.Utils.exclusiveInclusive
import me.owdding.skyocean.utils.Utils.powderForInterval
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import me.owdding.skyocean.utils.Utils.totalPowder
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.profile.ProfileChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.item.getVisualItem
import tech.thatgravyboat.skyblockapi.api.profile.skilltree.SkillTreeAPI
import tech.thatgravyboat.skyblockapi.api.profile.skilltree.SkillTreeCurrency
import tech.thatgravyboat.skyblockapi.api.profile.skilltree.SkillTreeData
import tech.thatgravyboat.skyblockapi.api.profile.skilltree.SkillTreePerk
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTagKey
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

abstract class SkillTreeHelper<Powder: SkillTreeCurrency, Data : SkillTreeData<Perk>, Perk : SkillTreePerk, Self : SkillTreeAPI<Data, Perk, Self>>(
    val reminders: Map<Powder, String>,
    val api: SkillTreeAPI<Data, Perk, Self>,
    val inventoryTitle: String,
    val perkItems: ItemTagKey,
    val config: SkillTreeConfig,
) {

    protected val cachedPerkCost = mutableMapOf<Powder, Int>()
    protected var lastClick = Instant.DISTANT_PAST

    protected abstract fun Powder.getCurrentAmount(): Long?


    protected fun tryReplaceItem(item: ItemStack) {
        val perkName = item.cleanName
        val perkByName = TreeRepoData.hotfByName(perkName) as? LevelingTreeNode ?: return
        val tooltipLines = item.getLore()
        val isLocked = item.getItemModel() == Items.PALE_OAK_BUTTON || item.getVisualItem() == Items.COAL
        val notEnoughCurrency = tooltipLines.any { it.stripped.startsWith("you don't have enough ", true) }
        val level = tooltipLines.firstOrNull()?.let {
            val isBoosted = it.siblings.any { sibling -> sibling.style.color?.serialize() == "aqua" }
            val level = it.stripped.substringBefore("/").filter { c -> c.isDigit() }.toInt()
            if (isBoosted) level - 1 else level
        } ?: 1
        if (isLocked && !config.totalProgress) {
            return
        }

        item.skyoceanReplace {
            if (config.stackSize && !isLocked) {
                customSlotText = level.toString()
            }
            tooltip {
                val listMerger = ListMerger(tooltipLines)

                if (config.totalProgress) {
                    listMerger.addAfterNext({ it.stripped.startsWith("Level") }) {
                        add(
                            Text.of {
                                val currencySpent = perkByName.powderForInterval(1 exclusiveInclusive level)
                                val totalCurrencyNeeded = perkByName.totalPowder()
                                this.color = TextColor.DARK_GRAY
                                append("WHISPERS/POWDER ${currencySpent.toFormattedString()}") { this.color = TextColor.GRAY }
                                append("/${totalCurrencyNeeded.toFormattedString()}")
                                append(CommonComponents.SPACE)
                                append(
                                    Text.of(((currencySpent.toFloat() / totalCurrencyNeeded) * 100).round()) {
                                        append("%")
                                        this.color = TextColor.YELLOW
                                    }.wrap("(", ")"),
                                )
                            },
                        )
                    }
                }

                if (level != 0 && (config.displayTotalLeft || config.displayShiftCost) && !isLocked) {
                    listMerger.addBeforeNext({ it.stripped in listOf("ENABLED", "DISABLED") }) {
                        fun MutableList<Component>.add(levels: Int) {
                            val name = perkByName.powderType.displayName ?: return
                            val formatting = perkByName.powderType.formatting ?: ChatFormatting.WHITE
                            add("Cost (") {
                                append(levels.toFormattedString()) { this.color = TextColor.YELLOW }
                                append(")")
                                this.color = TextColor.GRAY
                            }
                            add(perkByName.powderForInterval(level exclusiveInclusive (level + levels)).toFormattedString()) {
                                append(CommonComponents.SPACE)
                                append(name)
                                this.withStyle(formatting)
                            }
                            add(CommonComponents.EMPTY)
                        }
                        if (level + 1 >= perkByName.maxLevel) return@addBeforeNext

                        val levelsOnShiftClick = perkByName.maxLevel.minus(level).coerceAtMost(10)
                        if (config.displayShiftCost) {
                            add(levelsOnShiftClick)
                        }

                        if (config.displayShiftCost && levelsOnShiftClick < 10) return@addBeforeNext
                        val levelsUntilMax = perkByName.maxLevel.minus(level)
                        if (config.displayShiftCost && levelsUntilMax == 10) return@addBeforeNext
                        if (config.displayTotalLeft) {
                            add(levelsUntilMax)
                        }
                    }
                }

                listMerger.addRemaining()
                if (!perkByName.isMaxed(level) && !isLocked && config.reminder && notEnoughCurrency) run {
                    val (costType, amount) = perkByName.costForLevel(level + 1)
                    //if (costType.type != CostTypes.WHISPER) return@run
                    val currencyType = (costType as WhisperCostType).whisperType // (costType as PowderCostType).powderType
                    val currentPerk = reminders[currencyType]
                    if (currentPerk != perkName) {
                        Text.of {
                            append(ChatUtils.ICON_SPACE_COMPONENT)
                            append("Click to set a reminder when you", OceanColors.SKYOCEAN_BLUE)
                            append(CommonText.NEWLINE)
                            append("have enough WHISPERS/POWDER to buy this perk!", OceanColors.SKYOCEAN_BLUE)
                        }.splitLines().forEach(listMerger::add)
                        this@skyoceanReplace.onClick { button ->
                            if (button != InputConstants.MOUSE_BUTTON_LEFT) return@onClick null
                            PerkUpgradeStorage[currencyType] = perkName
                            cachedPerkCost[currencyType] = amount
                            // This is needed so that the lore gets updated when we toggle the reminder
                            tryReplaceItem(item)
                            lastClick = currentInstant()
                        }
                    } else {
                        Text.of {
                            append(ChatUtils.ICON_SPACE_COMPONENT)
                            append("Click to remove reminder!", OceanColors.SKYOCEAN_BLUE)
                        }.let(listMerger::add)

                        backgroundItem = Items.BLUE_STAINED_GLASS_PANE.defaultInstance

                        this@skyoceanReplace.onClick { button ->
                            if (button != InputConstants.MOUSE_BUTTON_LEFT) return@onClick null
                            PerkUpgradeStorage.remove(currencyType)
                            cachedPerkCost.remove(currencyType)
                            tryReplaceItem(item)
                            lastClick = currentInstant()
                        }
                    }
                }
                listMerger.applyToTooltip(this)
            }
        }
    }

    protected fun getNextLevelCost(perkName: String): Int? {
        val perk = TreeRepoData.hotmByName(perkName) as? LevelingTreeNode ?: return null
        val level = api.perks[perkName]?.level ?: return null
        return perk.costForLevel(level + 1).second
    }

    @Subscription(TickEvent::class, inherited = true)
    @TimePassed("1s")
    @OnlyOnSkyBlock
    fun onTick() {
        val reminders = reminders
        // If there's a perk we dont have the cost for, recalculate the cost
        if (reminders.keys != cachedPerkCost.keys) {
            reminders.forEach { (powder, perkName) ->
                val cost = getNextLevelCost(perkName) ?: return@forEach
                cachedPerkCost[powder] = cost
            }
        }
        val powders = cachedPerkCost.filter { (powder, needed) ->
            val current = powder.getCurrentAmount() ?: return@filter false
            current >= needed
        }.keys
        if (powders.isEmpty()) return
        if (lastClick.since() < 5.seconds) return
        val perks = reminders.filterKeys { it in powders }
        // We remove the perks even if you have the feature disabled
        powders.forEach {
            PerkUpgradeStorage.remove(it)
            cachedPerkCost.remove(it)
        }
        if (!config.reminder) return
        perks.forEach { (type, perk) ->
            Text.of {
                append("You have enough ")
                append(type.displayname)
                append(" to upgrade your $perk perk!")

                hover = Text.of("Click to open the Hotm menu")
                onClick {
                    McClient.sendCommand("hotm")
                }
            }.sendWithPrefix()
        }
        if (!config.reminderTitle) return
        val title = Text.of {
            append(ChatUtils.ICON_SPACE_COMPONENT)
            append("Hotm perk upgrade!") {
                color = OceanColors.WARNING
                bold = true
            }
        }
        McClient.setTitle(title)
    }

    // The reminded perks are different per profile and the levels are different, meaning that the cache needs to be updated
    @Subscription(ProfileChangeEvent::class, inherited = true)
    fun onProfileChange() = cachedPerkCost.clear()

    @Subscription(inherited = true)
    @OnlyOnSkyBlock
    fun onInventoryUpdate(event: InventoryChangeEvent) {
        if (!config.displayShiftCost && !config.displayTotalLeft && !config.totalProgress && !config.stackSize) return
        if (event.title != inventoryTitle) return
        if (event.isInPlayerInventory) return
        if (event.item !in perkItems) return
        tryReplaceItem(event.item)
    }

}
