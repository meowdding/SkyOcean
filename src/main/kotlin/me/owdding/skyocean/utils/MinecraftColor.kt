package me.owdding.skyocean.utils

import net.minecraft.ChatFormatting
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName

enum class MinecraftColor(val formatting: ChatFormatting, val paneItem: Item, val dye: DyeColor) {
    WHITE(ChatFormatting.WHITE, Items.WHITE_STAINED_GLASS_PANE, DyeColor.WHITE),
    ORANGE(ChatFormatting.GOLD, Items.ORANGE_STAINED_GLASS_PANE, DyeColor.ORANGE),
    MAGENTA(ChatFormatting.WHITE, Items.MAGENTA_STAINED_GLASS_PANE, DyeColor.MAGENTA), // chat no color
    LIGHT_BLUE(ChatFormatting.BLUE, Items.LIGHT_BLUE_STAINED_GLASS_PANE, DyeColor.LIGHT_BLUE),
    YELLOW(ChatFormatting.YELLOW, Items.YELLOW_STAINED_GLASS_PANE, DyeColor.YELLOW),
    LIME(ChatFormatting.GREEN, Items.LIME_STAINED_GLASS_PANE, DyeColor.LIME),
    PINK(ChatFormatting.LIGHT_PURPLE, Items.PINK_STAINED_GLASS_PANE, DyeColor.PINK),
    GRAY(ChatFormatting.DARK_GRAY, Items.GRAY_STAINED_GLASS_PANE, DyeColor.GRAY),
    LIGHT_GRAY(ChatFormatting.GRAY, Items.LIGHT_GRAY_STAINED_GLASS_PANE, DyeColor.LIGHT_GRAY),
    CYAN(ChatFormatting.AQUA, Items.CYAN_STAINED_GLASS_PANE, DyeColor.CYAN),
    PURPLE(ChatFormatting.DARK_PURPLE, Items.PURPLE_STAINED_GLASS_PANE, DyeColor.PURPLE),
    BLUE(ChatFormatting.DARK_BLUE, Items.BLUE_STAINED_GLASS_PANE, DyeColor.BLUE),
    BROWN(ChatFormatting.WHITE, Items.BROWN_STAINED_GLASS_PANE, DyeColor.BROWN), // no chat color
    GREEN(ChatFormatting.DARK_GREEN, Items.GREEN_STAINED_GLASS_PANE, DyeColor.GREEN),
    RED(ChatFormatting.RED, Items.RED_STAINED_GLASS_PANE, DyeColor.RED),
    BLACK(ChatFormatting.BLACK, Items.BLACK_STAINED_GLASS_PANE, DyeColor.BLACK),
    ;

    val paneStack: ItemStack = paneItem.defaultInstance

    override fun toString() = "§${formatting.char}${toFormattedName()}"

}
