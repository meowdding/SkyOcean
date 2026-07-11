package me.owdding.skyocean.utils

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

enum class MinecraftColor(val dye: DyeColor, val paneItem: Item) : Translatable {
    WHITE(DyeColor.WHITE, Items.STAINED_GLASS_PANE.white()),
    ORANGE(DyeColor.ORANGE, Items.STAINED_GLASS_PANE.orange()),
    MAGENTA(DyeColor.MAGENTA, Items.STAINED_GLASS_PANE.magenta()),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, Items.STAINED_GLASS_PANE.lightBlue()),
    YELLOW(DyeColor.YELLOW, Items.STAINED_GLASS_PANE.yellow()),
    LIME(DyeColor.LIME, Items.STAINED_GLASS_PANE.lime()),
    PINK(DyeColor.PINK, Items.STAINED_GLASS_PANE.pink()),
    GRAY(DyeColor.GRAY, Items.STAINED_GLASS_PANE.gray()),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY, Items.STAINED_GLASS_PANE.lightGray()),
    CYAN(DyeColor.CYAN, Items.STAINED_GLASS_PANE.cyan()),
    PURPLE(DyeColor.PURPLE, Items.STAINED_GLASS_PANE.purple()),
    BLUE(DyeColor.BLUE, Items.STAINED_GLASS_PANE.blue()),
    BROWN(DyeColor.BROWN, Items.STAINED_GLASS_PANE.brown()),
    GREEN(DyeColor.GREEN, Items.STAINED_GLASS_PANE.green()),
    RED(DyeColor.RED, Items.STAINED_GLASS_PANE.red()),
    BLACK(DyeColor.BLACK, Items.STAINED_GLASS_PANE.black()),
    ;

    // ARGB, opaque
    val color: Int = dye.textureDiffuseColor

    val paneStack: ItemStack by lazy { paneItem.defaultInstance }

    override fun getTranslationKey(): String = "skyocean.minecraft_colors.${name.lowercase()}"

}
