package me.owdding.skyocean.features.item.lore

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.lorecleanup.LoreModifierConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
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
        add {
            val colorText = {
                append("Color: ") { this.color = TextColor.DARK_GRAY }
            }

            val hexText = {
                append(String.format("#%06X", dyeColor.rgb)) {
                    this.color = TextColor.DARK_GRAY
                }
            }

            val square = {
                append("█") { this.color = dyeColor.rgb }
            }

            when (LoreModifierConfig.dyePosition) {
                DyePosition.LEFT -> {
                    square()
                    append(" ")
                    colorText()
                    hexText()
                }

                DyePosition.MIDDLE -> {
                    colorText()
                    square()
                    append(" ")
                    hexText()
                }

                DyePosition.RIGHT -> {
                    colorText()
                    hexText()
                    append(" ")
                    square()
                }
            }
        }

        Result.modified
    }

    enum class DyePosition : Translatable {
        LEFT,
        MIDDLE,
        RIGHT,
        ;

        override fun getTranslationKey() = "skyocean.config.lore_modifiers.dye_pos.${name.lowercase()}"
    }
}
