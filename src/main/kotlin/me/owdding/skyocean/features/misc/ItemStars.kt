package me.owdding.skyocean.features.misc

import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@ItemModifier
object StarStackSizeModifier : AbstractItemModifier() {
    override val displayName: Component get() = Text.translatable("skyocean.config.misc.itemStarStacksize")
    override val isEnabled: Boolean get() = MiscConfig.itemStarStacksize

    override fun appliesTo(itemStack: ItemStack): Boolean {
        itemStack[DataTypes.STAR_COUNT]?.takeUnless { it == 0 } ?: return false
        return true
    }

    override fun itemCountOverride(itemStack: ItemStack): Component? {
        val stars = itemStack[DataTypes.STAR_COUNT]?.takeUnless { it == 0 } ?: return null
        val color = when {
            stars >= 6 -> TextColor.RED
            else -> TextColor.ORANGE
        }
        return Text.of("$stars", color)
    }
}

@ItemModifier
object RevertMasterStarModifier : AbstractItemModifier() {
    override val displayName: Component get() = Text.translatable("skyocean.config.misc.revertMasterStars")
    override val isEnabled: Boolean get() = MiscConfig.revertMasterStars

    override fun appliesTo(itemStack: ItemStack): Boolean {
        val stars = itemStack[DataTypes.STAR_COUNT] ?: return false
        return stars > 5
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result {
        // CHANGES THE WEIRD ROUNDED NUMBER TO THE OLD STACKED STAR COLORS

    }
}
