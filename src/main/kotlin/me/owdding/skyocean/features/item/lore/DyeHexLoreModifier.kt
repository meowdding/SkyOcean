package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.add
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@ItemModifier
object DyeHexLoreModifier : AbstractItemModifier() {
    override val displayName: Component = +"skyocean.config.lore_modifiers.dye_hex"
    override val isEnabled: Boolean get() = LoreModifierConfig.dyeHex


    override fun appliesTo(itemStack: ItemStack): Boolean {
        val isDyedHidden = itemStack.get(DataComponents.TOOLTIP_DISPLAY)?.shows(DataComponents.DYED_COLOR) == false
        val isLeather = itemStack.has(DataComponents.DYED_COLOR)
        return isDyedHidden && isLeather
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
        val dyeColor = item.get(DataComponents.DYED_COLOR) ?: return@withMerger null

        copy()
        add("Color: ${String.format("#%06X", dyeColor.rgb)}") {
            this.color = TextColor.DARK_GRAY
        }
        Result.modified
    }
}
