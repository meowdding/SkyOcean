package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperStorage.setSelected
import me.owdding.skyocean.utils.ChatUtils
import me.owdding.skyocean.utils.Utils.contains
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object RecipeModifier {


    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!MiscConfig.craftHelperEnabled) return
        if (event.slot.index != 14) return
        if (event.inventory.size < 23 || event.inventory[23].item !in Items.CRAFTING_TABLE) return
        if (event.inventory.size < 32 || event.inventory[32].item.cleanName != "Supercraft") return
        val item = event.inventory[25].item.getSkyBlockId() ?: return
        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to place craft helper item in recipe, item is not a glass pane")
            return
        }
        event.item.replaceVisually {
            set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true))
            this.item = Items.DIAMOND_PICKAXE
            name(
                Text.join(ChatUtils.ICON_SPACE_COMPONENT, "Craft Helper") {
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
