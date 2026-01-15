package me.owdding.skyocean.features.misc

import com.teamresourceful.resourcefullib.common.color.ConstantColors
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.regex.component.ComponentRegex
import tech.thatgravyboat.skyblockapi.utils.regex.component.matchOrNull
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@ItemModifier
object StarStackSizeModifier : AbstractItemModifier() {
    override val displayName: Component get() = Text.translatable("skyocean.config.misc.itemStarStacksize")
    override val isEnabled: Boolean get() = MiscConfig.itemStarStacksize

    override fun appliesTo(itemStack: ItemStack): Boolean 
        = itemStack[DataTypes.STAR_COUNT]?.takeUnless { it == 0 } != null

    override fun itemCountOverride(itemStack: ItemStack): Component? {
        val stars = itemStack[DataTypes.STAR_COUNT]?.takeUnless { it == 0 } ?: return null
        val isDungeonItem = itemStack[DataTypes.CATEGORY]?.isDungeon ?: return null

        val color = if (isDungeonItem) {
            when (stars) {
                1, 2 -> ConstantColors.lightyellow
                3 -> ConstantColors.yellow
                4 -> ConstantColors.orange
                5 -> ConstantColors.darkorange
                6, 7 -> ConstantColors.orangered
                8 -> ConstantColors.red
                9 -> ConstantColors.indianred
                else if stars >= 10 -> ConstantColors.darkred
                else -> null
            }
        } else {
            when (stars) {
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
                else if stars >= 15 -> ConstantColors.steelblue
                else -> null
            }
        }?.value ?: return null
        return Text.of("$stars", color)
    }
}

@ItemModifier
object RevertMasterStarModifier : AbstractItemModifier() {
    override val displayName: Component get() = Text.translatable("skyocean.config.misc.revertMasterStars")
    override val isEnabled: Boolean get() = MiscConfig.revertMasterStars
    private val regex = ComponentRegex("(?<first>.*)✪✪✪✪✪[➊➋➌➍➎](?<second>.*)")

    override fun appliesTo(itemStack: ItemStack): Boolean {
        val stars = itemStack[DataTypes.STAR_COUNT] ?: return false
        val category = itemStack[DataTypes.CATEGORY] ?: return false
        return stars > 5 && category.isDungeon
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?): Result {
        val stars = item[DataTypes.STAR_COUNT]?.takeIf { it > 5 }?.minus(5) ?: return Result.unmodified
        return withMerger(list) {
            val name = read()
            val newName = regex.matchOrNull(name, "first", "second") { (first, second) ->
                Text.of {
                    append(first)
                    repeat(stars) {
                        append("✪", TextColor.RED)
                    }
                    repeat(5 - stars) {
                        append("✪", TextColor.ORANGE)
                    }
                    append(second)
                }
            } ?: name
            add(newName)


            if (newName === name) Result.unmodified else Result.modified
        }
    }
}
