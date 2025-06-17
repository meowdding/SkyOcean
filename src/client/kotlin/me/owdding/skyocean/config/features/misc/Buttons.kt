package me.owdding.skyocean.config.features.misc

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.config.ConfigCategory
import org.intellij.lang.annotations.Language

@ConfigCategory
object Buttons : CategoryKt("buttons") {
    override val hidden: Boolean = true

    override val name = Translated("config.titanomachy.buttons.title")

    val button0: ButtonConfig = obj("button0", ButtonConfig("minecraft:diamond_sword", "Skills", "^Your Skills$"))
    val button1: ButtonConfig = obj("button1", ButtonConfig("minecraft:painting", "Collections",  "^Collections$"))
    val button2: ButtonConfig = obj("button2", ButtonConfig("minecraft:bone", "Pets", "Pets(?: \\(\\d+/\\d+\\))?"))
    val button3: ButtonConfig = obj("butto3", ButtonConfig("ARMOR_OF_YOG_CHESTPLATE", "Wardrobe", "^Wardrobe(?: \\(\\d+/\\d+\\))?"))
    val button4: ButtonConfig = obj("button4", ButtonConfig("minecraft:bundle", "Sacks", "^Sack of Sacks$"))
    val button5: ButtonConfig = obj("button5", ButtonConfig("RUNEBOOK", "Accessories", "^Accessory Bag(?: \\(\\d+/\\d+\\))?"))
    val button6: ButtonConfig = obj("button6", ButtonConfig("minecraft:ender_chest", "Storage", "^Storage$"))
    val button7: ButtonConfig = obj("button7", ButtonConfig("minecraft:grass_block", "warp island", "a^", "Island"))
    val button8: ButtonConfig = obj("button8", ButtonConfig("HUB_PORTAL", "Hub", "a^"))
    val button9: ButtonConfig = obj("button9", ButtonConfig("minecraft:skeleton_skull", "Dungeons", "a^"))
    val button10: ButtonConfig = obj("button10", ButtonConfig("SMOOTH_CHOCOLATE_BAR", "ChocolateFactory", "^Chocolate Factory$", "Chocolate Factory"))
    val button11: ButtonConfig = obj("button11", ButtonConfig("ESSENCE_GOLD", "Bazaar", "^(Special )?Bazaar$"))
    val button12: ButtonConfig = obj("button12", ButtonConfig("ESSENCE_DIAMOND", "Auction", "^(Co-op )?Auction House$"))
    val button13: ButtonConfig = obj("button13", ButtonConfig("minecraft:crafting_table", "CraftingTable", "^Craft Item$", "Crafting Table"))

    val buttons get() = arrayOf(button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, button11, button12, button13)

}

class ButtonConfig(itemName: String, command: String, @Language("RegExp") title: String, tooltip: String = "") : ObjectKt() {

    var item by string(itemName)
    var command by string(command)
    var title by string(title)
    var tooltip by string(tooltip) }

