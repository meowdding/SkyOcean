package me.owdding.skyocean.features.foraging

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ListMerger
import me.owdding.lib.extensions.add
import me.owdding.lib.extensions.applyToTooltip
import me.owdding.lib.extensions.round
import me.owdding.lib.repo.LevelingTreeNode
import me.owdding.lib.repo.TreeRepoData
import me.owdding.skyocean.config.features.foraging.ForagingConfig
import me.owdding.skyocean.utils.Utils.exclusiveInclusive
import me.owdding.skyocean.utils.Utils.powderForInterval
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import me.owdding.skyocean.utils.Utils.totalPowder
import me.owdding.skyocean.utils.tags.ItemModelTagKey
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getItemModel
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color


@Module
object HotfHelper {

    @Subscription
    @OnlyOnSkyBlock
    fun onInventoryUpdate(event: InventoryChangeEvent) {
        val config = ForagingConfig
        if (!config.hotfDisplayTotalLeft && !config.hotfDisplayShiftCost && !config.hotfTotalProgress && !config.hotfStackSize) return
        if (event.title != "Heart of the Forest") return
        if (event.isInPlayerInventory) return
        if (event.item !in ItemModelTagKey.HOTM_PERK_ITEMS) return
        val perkByName = TreeRepoData.hotfByName(event.item.cleanName) as? LevelingTreeNode ?: return
        val tooltipLines = event.item.getLore()
        val isLocked = event.item.getItemModel() == Items.PALE_OAK_BUTTON
        val level = tooltipLines.firstOrNull()?.stripped?.substringBefore("/")?.filter { c -> c.isDigit() }?.toInt() ?: 1
        if (isLocked && !config.hotfTotalProgress) {
            return
        }

        event.item.skyoceanReplace {
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
                            val formatting = perkByName.powderType.formatting
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
                listMerger.applyToTooltip(this)
            }
        }
    }
}

