package me.owdding.skyocean.features.mining

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ListMerger
import me.owdding.lib.extensions.add
import me.owdding.lib.extensions.applyToTooltip
import me.owdding.lib.extensions.round
import me.owdding.lib.repo.LevelingTreeNode
import me.owdding.lib.repo.TreeRepoData
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.utils.Utils.exclusiveInclusive
import me.owdding.skyocean.utils.Utils.powderForInterval
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import me.owdding.skyocean.utils.Utils.totalPowder
import me.owdding.skyocean.utils.tags.ItemTagKey
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object HotmHelper {

    @Subscription
    @OnlyOnSkyBlock
    fun onInventoryUpdate(event: InventoryChangeEvent) {
        if (!MiningConfig.hotmDisplayTotalLeft && !MiningConfig.hotmDisplayShiftCost && !MiningConfig.hotmTotalProgress && !MiningConfig.hotmStackSize) return
        if (event.title != "Heart of the Mountain") return
        if (event.isInPlayerInventory) return
        if (event.item !in ItemTagKey.HOTM_PERK_ITEMS) return
        val perkByName = TreeRepoData.hotmByName(event.item.cleanName) as? LevelingTreeNode ?: return
        val tooltipLines = event.item.getLore()
        val isLocked = event.item.item == Items.COAL
        val level = tooltipLines.firstOrNull()?.let {
            val isBoosted = it.siblings.any { sibling -> sibling.style.color?.serialize() == "aqua" }
            val level = it.stripped.substringBefore("/").filter { c -> c.isDigit() }.toInt()

            if (isBoosted) level - 1 else level
        } ?: 1
        if (isLocked && !MiningConfig.hotmTotalProgress) {
            return
        }

        event.item.skyoceanReplace {
            if (MiningConfig.hotmStackSize && !isLocked) {
                customSlotText = level.toString()
            }
            tooltip {
                val listMerger = ListMerger(tooltipLines)

                if (MiningConfig.hotmTotalProgress) {
                    listMerger.addAfterNext({ it.stripped.startsWith("Level") }) {
                        add(
                            Text.of {
                                val powderSpent = perkByName.powderForInterval(1 exclusiveInclusive level)
                                val totalPowderNeeded = perkByName.totalPowder()
                                this.color = TextColor.DARK_GRAY
                                append("Powder ${powderSpent.toFormattedString()}") { this.color = TextColor.GRAY }
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

                if (level != 0 && (MiningConfig.hotmDisplayTotalLeft || MiningConfig.hotmDisplayShiftCost) && !isLocked) {
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
                        if (MiningConfig.hotmDisplayShiftCost) {
                            add(levelsOnShiftClick)
                        }

                        if (MiningConfig.hotmDisplayShiftCost && levelsOnShiftClick < 10) return@addBeforeNext
                        val levelsUntilMax = perkByName.maxLevel.minus(level)
                        if (MiningConfig.hotmDisplayShiftCost && levelsUntilMax == 10) return@addBeforeNext
                        if (MiningConfig.hotmDisplayTotalLeft) {
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
