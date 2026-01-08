package me.owdding.skyocean.features.item

import com.teamresourceful.resourcefullib.common.color.ConstantColors
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.not
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.text.Text

@ItemModifier
class ItemStarModifier : AbstractItemModifier() {
    override val displayName: Component = !"star modifier"
    override val isEnabled: Boolean = true
    override val modifierSources: List<ModifierSource> = ModifierSource.entries.toMutableList().apply {
        remove(ModifierSource.HOTBAR)
    }

    override fun appliesTo(itemStack: ItemStack): Boolean = itemStack[DataTypes.STAR_COUNT] != null && McScreen.self != null

    override fun itemCountOverride(itemStack: ItemStack): Component? {
        val starCount = itemStack[DataTypes.STAR_COUNT] ?: return null
        val isDungeonItem = itemStack[DataTypes.CATEGORY]?.isDungeon ?: return null

        val color = if (isDungeonItem) {
            when (starCount) {
                1, 2 -> ConstantColors.lightyellow
                3 -> ConstantColors.yellow
                4 -> ConstantColors.orange
                5 -> ConstantColors.darkorange
                6, 7 -> ConstantColors.orangered
                8 -> ConstantColors.red
                9 -> ConstantColors.indianred
                10 -> ConstantColors.darkred
                else -> null
            }
        } else {
            when (starCount) {
                1, 2 -> ConstantColors.lightyellow
                3 -> ConstantColors.yellow
                4 -> ConstantColors.orange
                5 -> ConstantColors.darkorange
                6, 7 -> ConstantColors.lightpink
                8 -> ConstantColors.hotpink
                9 -> ConstantColors.deeppink
                10 -> ConstantColors.pink
                11 -> ConstantColors.lightcyan
                12 -> ConstantColors.cyan
                13 -> ConstantColors.lightskyblue
                14 -> ConstantColors.cornflowerblue
                15 -> ConstantColors.steelblue
                else -> null
            }
        }?.value ?: return null

        return Text.of(starCount.toString()) {
            withColor(color)
        }
    }
}
