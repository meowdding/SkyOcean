package me.owdding.skyocean.features.recipe.crafthelper.modifiers

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.ktmodules.Module
import me.owdding.lib.compat.REIRuntimeCompatability
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage.setSelected
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.Utils.skyoceanReplace
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object RecipeModifier {
    val SET_CRAFTHELPER_KEYBIND = SkyOceanKeybind("skyocean.keybind.crafthelper", InputConstants.KEY_V)

    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!MiscConfig.craftHelperEnabled) return
        if (event.slot.index != 14) return
        if (event.inventory.size < 23 || event.inventory[23].item !in Items.CRAFTING_TABLE) return
        if (event.inventory.size < 32 || event.inventory[32].item.cleanName != "Supercraft") return
        val item = SkyOceanItemId.fromItem(event.inventory[25].item) ?: return
        if (event.item !in ItemTag.GLASS_PANES) {
            SkyOcean.warn("Failed to place craft helper item in recipe, item is not a glass pane")
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

    @Subscription
    fun onKeybind(event: ScreenKeyReleasedEvent.Pre) {
        if (!SET_CRAFTHELPER_KEYBIND.matches(event)) return

        val reiHovered = REIRuntimeCompatability.getReiHoveredItemStack()
        val mcScreenHovered = McScreen.asMenu?.getHoveredSlot()?.item?.takeUnless { it.isEmpty }
        val item = mcScreenHovered ?: reiHovered ?: return

        setSelected(SkyOceanItemId.fromItem(item))
        McScreen.self?.let { it.resize(McClient.self, it.width, it.height) }

        Text.of("Set selected Crafthelper item to ") {
            append(item.cleanName) {
                this.color = TextColor.GOLD
                this.bold = true
            }
        }.sendWithPrefix()
    }

}
