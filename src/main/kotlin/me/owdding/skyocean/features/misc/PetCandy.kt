package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ListMerger
import me.owdding.lib.extensions.applyToTooltip
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object PetCandy {

    @Subscription
    @OnlyOnSkyBlock
    fun onInv(event: InventoryChangeEvent) {
        if (!MiscConfig.showHiddenPetCandy) return
        val item = event.item
        if (item.getSkyBlockId() != "PET") return

        if (!item.getRawLore().contains("MAX LEVEL")) return
        val candy = item.getData(DataTypes.PET_DATA)?.candyUsed?.takeUnless { it == 0 } ?: return

        item.skyoceanReplace {
            tooltip {
                val merger = ListMerger(event.item.getLore())

                merger.addBeforeNext({ it.stripped == "MAX LEVEL" }) {
                    add(
                        Text.of {
                            color = TextColor.GREEN
                            append("(")
                            append("$candy")
                            append("/10) Pet Candy Used")
                        },
                    )
                    add(CommonText.EMPTY)
                }

                merger.addRemaining()
                merger.applyToTooltip(this)
            }
        }
    }

}
