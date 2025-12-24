package me.owdding.skyocean.utils

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

enum class MinecraftColor(val dye: DyeColor, val paneItem: Item) : Translatable {
    WHITE(DyeColor.WHITE, Items.WHITE_STAINED_GLASS_PANE),
    ORANGE(DyeColor.ORANGE, Items.ORANGE_STAINED_GLASS_PANE),
    MAGENTA(DyeColor.MAGENTA, Items.MAGENTA_STAINED_GLASS_PANE),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_STAINED_GLASS_PANE),
    YELLOW(DyeColor.YELLOW, Items.YELLOW_STAINED_GLASS_PANE),
    LIME(DyeColor.LIME, Items.LIME_STAINED_GLASS_PANE),
    PINK(DyeColor.PINK, Items.PINK_STAINED_GLASS_PANE),
    GRAY(DyeColor.GRAY, Items.GRAY_STAINED_GLASS_PANE),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_STAINED_GLASS_PANE),
    CYAN(DyeColor.CYAN, Items.CYAN_STAINED_GLASS_PANE),
    PURPLE(DyeColor.PURPLE, Items.PURPLE_STAINED_GLASS_PANE),
    BLUE(DyeColor.BLUE, Items.BLUE_STAINED_GLASS_PANE),
    BROWN(DyeColor.BROWN, Items.BROWN_STAINED_GLASS_PANE),
    GREEN(DyeColor.GREEN, Items.GREEN_STAINED_GLASS_PANE),
    RED(DyeColor.RED, Items.RED_STAINED_GLASS_PANE),
    BLACK(DyeColor.BLACK, Items.BLACK_STAINED_GLASS_PANE),
    ;

    // ARGB, opaque
    val color: Int = dye.textureDiffuseColor

    val paneStack: ItemStack = paneItem.defaultInstance

    override fun getTranslationKey(): String = "skyocean.minecraft_colors.${name.lowercase()}"

}
