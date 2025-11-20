package me.owdding.skyocean.config.features.inventory

import kotlin.jvm.optionals.getOrNull
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import me.owdding.skyocean.utils.Utils.id
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.intellij.lang.annotations.Language
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

object Buttons : CategoryKt("buttons") {
    override val hidden: Boolean = true

    val button0: ButtonConfig by obj(ButtonConfig(Items.DIAMOND_SWORD, "Skills", "Your Skills"))
    val button1: ButtonConfig by obj(ButtonConfig(Items.PAINTING, "Collections", "Collections"))
    val button2: ButtonConfig by obj(ButtonConfig(Items.BONE, "Pets", "Pets(?: \\(\\d+/\\d+\\))?"))
    val button3: ButtonConfig by obj(ButtonConfig("ARMOR_OF_YOG_CHESTPLATE", "Wardrobe", "Wardrobe(?: \\(\\d+/\\d+\\))?"))
    val button4: ButtonConfig by obj(ButtonConfig(Items.BUNDLE, "Sacks", "Sack of Sacks"))
    val button5: ButtonConfig by obj(ButtonConfig("RUNEBOOK", "Accessories", "Accessory Bag(?: \\(\\d+/\\d+\\))?"))
    val button6: ButtonConfig by obj(ButtonConfig(Items.ENDER_CHEST, "Storage", "Storage"))
    val button7: ButtonConfig by obj(ButtonConfig(Items.GRASS_BLOCK, "warp island", "a^", "Island"))
    val button8: ButtonConfig by obj(ButtonConfig("HUB_PORTAL", "Hub", "a^"))
    val button9: ButtonConfig by obj(ButtonConfig(Items.SKELETON_SKULL, "warp dh", "a^", "Dungeon Hub"))
    val button10: ButtonConfig by obj(ButtonConfig("SMOOTH_CHOCOLATE_BAR", "ChocolateFactory", "Chocolate Factory", "Chocolate Factory"))
    val button11: ButtonConfig by obj(ButtonConfig("ESSENCE_GOLD", "Bazaar", "(?:Special )?Bazaar"))
    val button12: ButtonConfig by obj(ButtonConfig("ESSENCE_DIAMOND", "Auction", "(?:Co-op )?Auction House"))
    val button13: ButtonConfig by obj(ButtonConfig(Items.CRAFTING_TABLE, "CraftingTable", "Craft Item", "Crafting Table"))

    val buttons
        get() = arrayOf(
            button0,
            button1,
            button2,
            button3,
            button4,
            button5,
            button6,
            button7,
            button8,
            button9,
            button10,
            button11,
            button12,
            button13,
        )

}

class ButtonConfig(
    private val itemName: String,
    private val commandName: String,
    @param:Language("RegExp") private val titleName: String,
    private val tooltipName: String = "",
) : ObjectKt() {

    constructor(itemLike: ItemLike, command: String, @Language("RegExp") title: String, tooltip: String = "") :
        this(itemLike.id.toString(), command, title, tooltip)

    var regex = Regex(titleName)
        private set

    var item by observable(string(itemName)) { _, new ->
        this.itemStack = toItem(new)
    }
    var command by string(commandName)
    var title by observable(string(titleName)) { _, new ->
        runCatching {
            this.regex = Regex(new)
        }
    }
    var tooltip by string(tooltipName)
    var disabled by boolean(false)

    var itemStack: ItemStack = toItem(item)

    fun reset() {
        item = itemName
        command = commandName
        title = titleName
        tooltip = tooltipName
        disabled = false
    }

    private fun toItem(id: String) = ResourceLocation.tryParse(id.lowercase())?.let {
        BuiltInRegistries.ITEM.getOptional(it).getOrNull()?.defaultInstance
    } ?: RepoItemsAPI.getItem(id.uppercase())
}

