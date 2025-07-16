package me.owdding.skyocean.utils

import net.minecraft.ChatFormatting
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName

enum class MinecraftColor(val formatting: ChatFormatting, val paneItem: Item) {
    WHITE(ChatFormatting.WHITE, Items.WHITE_STAINED_GLASS_PANE),
    ORANGE(ChatFormatting.GOLD, Items.ORANGE_STAINED_GLASS_PANE),
    MAGENTA(ChatFormatting.WHITE, Items.MAGENTA_STAINED_GLASS_PANE), // chat no color
    LIGHT_BLUE(ChatFormatting.BLUE, Items.LIGHT_BLUE_STAINED_GLASS_PANE),
    YELLOW(ChatFormatting.YELLOW, Items.YELLOW_STAINED_GLASS_PANE),
    LIME(ChatFormatting.GREEN, Items.LIME_STAINED_GLASS_PANE),
    PINK(ChatFormatting.LIGHT_PURPLE, Items.PINK_STAINED_GLASS_PANE),
    GRAY(ChatFormatting.DARK_GRAY, Items.GRAY_STAINED_GLASS_PANE),
    LIGHT_GRAY(ChatFormatting.GRAY, Items.LIGHT_GRAY_STAINED_GLASS_PANE),
    CYAN(ChatFormatting.AQUA, Items.CYAN_STAINED_GLASS_PANE),
    PURPLE(ChatFormatting.DARK_PURPLE, Items.PURPLE_STAINED_GLASS_PANE),
    BLUE(ChatFormatting.DARK_BLUE, Items.BLUE_STAINED_GLASS_PANE),
    BROWN(ChatFormatting.WHITE, Items.BROWN_STAINED_GLASS_PANE), // no chat color
    GREEN(ChatFormatting.DARK_GREEN, Items.GREEN_STAINED_GLASS_PANE),
    RED(ChatFormatting.RED, Items.RED_STAINED_GLASS_PANE),
    BLACK(ChatFormatting.BLACK, Items.BLACK_STAINED_GLASS_PANE);

    val paneStack = paneItem.defaultInstance

    override fun toString() = "§${formatting.char}${toFormattedName()}"


}
