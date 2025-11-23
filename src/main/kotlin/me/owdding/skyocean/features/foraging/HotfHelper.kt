package me.owdding.skyocean.features.foraging

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ListMerger
import me.owdding.lib.extensions.add
import me.owdding.lib.extensions.applyToTooltip
import me.owdding.lib.extensions.round
import me.owdding.lib.repo.*
import me.owdding.lib.repo.WhisperType.FOREST
import me.owdding.skyocean.config.features.foraging.ForagingConfig
import me.owdding.skyocean.data.profile.PerkUpgradeStorage
import me.owdding.skyocean.utils.Utils.exclusiveInclusive
import me.owdding.skyocean.utils.Utils.powderForInterval
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import me.owdding.skyocean.utils.Utils.totalPowder
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.tags.ItemModelTagKey
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
import tech.thatgravyboat.skyblockapi.api.profile.hotf.HotfAPI
import tech.thatgravyboat.skyblockapi.api.profile.hotf.WhispersAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
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

// TODO: merge hotm helper and hotf helper into one, or abstract them out
@Module
object HotfHelper {

    private val reminders get() = PerkUpgradeStorage.hotf
    private val cachedPerkCost = enumMapOf<WhisperType, Int>()
    private var lastClick: Instant = Instant.DISTANT_PAST

    private val config get() = ForagingConfig


    private fun getNextLevelCost(perkName: String): Int? {
        val perk = TreeRepoData.hotfByName(perkName) as? LevelingTreeNode ?: return null
        val level = HotfAPI.perks[perkName]?.level ?: return null
        return perk.costForLevel(level + 1).second
    }

    // The reminded perks are different per profile and the levels are different, meaning that the cache needs to be updated
    @Subscription(ProfileChangeEvent::class)
    fun onProfileChange() = cachedPerkCost.clear()

    @Subscription(TickEvent::class)
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
        val whispers = cachedPerkCost.filter { (powder, needed) ->
            val current = powder.getCurrentAmount() ?: return@filter false
            current >= needed
        }.keys
        if (whispers.isEmpty()) return
        if (lastClick.since() < 5.seconds) return
        val perks = reminders.filterKeys { it in whispers }
        // We remove the perks even if you have the feature disabled
        whispers.forEach {
            PerkUpgradeStorage.remove(it)
            cachedPerkCost.remove(it)
        }
        if (!config.hotfReminder) return
        perks.forEach { (type, perk) ->
            Text.of {
                append("You have enough ")
                append(type.displayName)
                append(" to upgrade your $perk perk!")

                hover = Text.of("Click to open the Hotf menu")
                onClick {
                    McClient.sendCommand("hotf")
                }
            }.sendWithPrefix()
        }
        if (!config.reminderTitle) return
        val title = Text.of {
            append(ChatUtils.ICON_SPACE_COMPONENT)
            append("Hotf perk upgrade!") {
                color = OceanColors.WARNING
                bold = true
            }
        }
        McClient.setTitle(title)
    }

    private fun tryReplaceItem(item: ItemStack) {
        val perkName = item.cleanName
        val perkByName = TreeRepoData.hotfByName(perkName) as? LevelingTreeNode ?: return
        val tooltipLines = item.getLore()
        val isLocked = item.getItemModel() == Items.PALE_OAK_BUTTON
        val notEnoughWhispers = tooltipLines.any { it.stripped.startsWith("you don't have enough ", true) }
        val level = tooltipLines.firstOrNull()?.stripped?.substringBefore("/")?.filter { c -> c.isDigit() }?.toInt() ?: 1
        if (isLocked && !config.hotfTotalProgress) {
            return
        }

        item.skyoceanReplace {
            if (config.hotfStackSize && !isLocked) {
                customSlotText = level.toString()
            }
            tooltip {
                val listMerger = ListMerger(tooltipLines)

                if (config.hotfTotalProgress) {
                    listMerger.addAfterNext({ it.stripped.startsWith("Level") }) {
                        add(
                            Text.of {
                                val powderSpent = perkByName.powderForInterval(1 exclusiveInclusive level)
                                val totalPowderNeeded = perkByName.totalPowder()
                                this.color = TextColor.DARK_GRAY
                                append("Whispers ${powderSpent.toFormattedString()}") { this.color = TextColor.GRAY }
                                append("/${totalPowderNeeded.toFormattedString()}")
                                append(CommonComponents.SPACE)
                                append(
                                    Text.of(((powderSpent.toFloat() / totalPowderNeeded) * 100).round()) {
                                        append("%")
                                        this.color = TextColor.YELLOW
                                    }.wrap("(", ")"),
                                )
                            },
                        )
                    }
                }

                if (level != 0 && (config.hotfDisplayTotalLeft || config.hotfDisplayShiftCost) && !isLocked) {
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
                        if (config.hotfDisplayShiftCost) {
                            add(levelsOnShiftClick)
                        }

                        if (config.hotfDisplayShiftCost && levelsOnShiftClick < 10) return@addBeforeNext
                        val levelsUntilMax = perkByName.maxLevel.minus(level)
                        if (config.hotfDisplayShiftCost && levelsUntilMax == 10) return@addBeforeNext
                        if (config.hotfDisplayTotalLeft) {
                            add(levelsUntilMax)
                        }
                    }
                }

                listMerger.addRemaining()
                if (!perkByName.isMaxed(level) && !isLocked && config.hotfReminder && notEnoughWhispers) run {
                    val (costType, amount) = perkByName.costForLevel(level + 1)
                    if (costType.type != CostTypes.WHISPER) return@run
                    val whisperType = (costType as WhisperCostType).whisperType
                    val currentPerk = reminders[whisperType]
                    if (currentPerk != perkName) {
                        Text.of {
                            append(ChatUtils.ICON_SPACE_COMPONENT)
                            append("Click to set a reminder when you", OceanColors.SKYOCEAN_BLUE)
                            append(CommonText.NEWLINE)
                            append("have enough whispers to buy this perk!", OceanColors.SKYOCEAN_BLUE)
                        }.splitLines().forEach(listMerger::add)
                        this@skyoceanReplace.onClick { button ->
                            if (button != InputConstants.MOUSE_BUTTON_LEFT) return@onClick null
                            PerkUpgradeStorage[whisperType] = perkName
                            cachedPerkCost[whisperType] = amount
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
                            PerkUpgradeStorage.remove(whisperType)
                            cachedPerkCost.remove(whisperType)
                            tryReplaceItem(item)
                            lastClick = currentInstant()
                        }
                    }
                }
                listMerger.applyToTooltip(this)
            }
        }
    }

    @Subscription
    @OnlyOnSkyBlock
    fun onInventoryUpdate(event: InventoryChangeEvent) {
        if (!config.hotfDisplayTotalLeft && !config.hotfDisplayShiftCost && !config.hotfTotalProgress && !config.hotfStackSize) return
        if (event.title != "Heart of the Forest") return
        if (event.isInPlayerInventory) return
        if (event.item !in ItemModelTagKey.HOTF_PERK_ITEMS) return
        tryReplaceItem(event.item)
    }

    // We default to null even if the `when` statement is currently exhaustive, since hypixel could add more whisper types in the future
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun WhisperType.getCurrentAmount(): Long? = when (this) {
        FOREST -> WhispersAPI.forest
        else -> null
    }
}

