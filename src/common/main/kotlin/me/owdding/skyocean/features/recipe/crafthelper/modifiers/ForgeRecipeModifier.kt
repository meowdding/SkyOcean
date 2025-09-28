package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage.setSelected
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object ForgeRecipeModifier {
    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!CraftHelperConfig.enabled) return
        if (event.title != "Confirm Process") return
        if (event.slot.index != 22 && event.slot.index != 23) return

        if (event.itemStacks[14] in Items.FURNACE && event.slot.index != 23) return
        if (event.itemStacks[13] in Items.FURNACE && event.slot.index != 22) return

        val item = SkyBlockId.fromItem(event.inventory[16].item) ?: return
        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to place craft helper item in forge recipe, item is not a glass pane")
            return
        }
        event.item.skyoceanReplace {
            set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true))
            this.item = Items.DIAMOND_PICKAXE
            name(
                Text.of("Craft Helper") {
                    this.color = TextColor.GREEN
                },
            )
            tooltip {
                add("Set as selected craft helper item!") {
                    this.color = TextColor.GRAY
                }
            }

            onClick {
                setSelected(item)
                McScreen.self?.let { it.resize(McClient.self, it.width, it.height) }
            }
        }
    }
}
