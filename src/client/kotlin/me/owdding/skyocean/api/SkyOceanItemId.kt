package me.owdding.skyocean.api

import me.owdding.skyocean.api.SkyOceanItemId.Companion.UNKNOWN
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@JvmInline
value class SkyOceanItemId private constructor(val id: String) {
    companion object {
        private val amountRegex = Regex(".*?x[\\d,]+")

        const val ITEM = "item:"
        const val PET = "pet:"
        const val RUNE = "rune:"
        const val ATTRIBUTE = "attribute:"
        const val ENCHANTMENT = "enchantment:"
        const val UNKNOWN = "ocean:unknown"

        fun item(id: String) = SkyOceanItemId("$ITEM$id")
        fun pet(id: String) = SkyOceanItemId("$PET$id")
        fun rune(id: String) = SkyOceanItemId("$RUNE$id")
        fun attribute(id: String) = SkyOceanItemId("$ATTRIBUTE$id")
        fun enchantment(id: String) = SkyOceanItemId("$ENCHANTMENT$id")

        fun fromItem(item: ItemStack) = item.getSkyOceanItemId()

        fun fromName(name: String): SkyOceanItemId? {
            var name = name.lowercase().stripColor()
            if (name.startsWith("[lvl")) {
                name = name.substringAfterLast("]")
            } else if (name.matches(amountRegex)) {
                name = name.substringBeforeLast(" x")
            }

            return SimpleItemApi.findIdByName(name)
        }

    }

    val isItem: Boolean get() = id.startsWith(ITEM)
    val isPet: Boolean get() = id.startsWith(PET)
    val isRune: Boolean get() = id.startsWith(RUNE)
    val isEnchantment: Boolean get() = id.startsWith(ENCHANTMENT)
    val isAttribute: Boolean get() = id.startsWith(ATTRIBUTE)

    fun toItem(): ItemStack = when {

        isRune -> getRune()
        isPet -> getPet()
        isItem -> getItem()
        isEnchantment -> getEnchantment()
        isAttribute -> getAttribute()

        else -> ItemStack(Items.BARRIER) {
            set(DataComponents.CUSTOM_NAME, Text.of(id) { this.color = TextColor.RED })
        }
    }

    private fun getItem(): ItemStack = SimpleItemApi.getItemById(this)
    private fun getPet(): ItemStack = SimpleItemApi.getPetById(this)
    private fun getRune(): ItemStack = SimpleItemApi.getRuneById(this)
    private fun getEnchantment(): ItemStack = SimpleItemApi.getEnchantmentById(this)
    private fun getAttribute(): ItemStack = SimpleItemApi.getAttributeById(this)

}

private fun ItemStack.getSkyOceanItemId(): SkyOceanItemId? {
    val data = this.getData(DataTypes.ID)
    return when (data) {
        "RUNE", "UNIQUE_RUNE" -> {
            this.getData(DataTypes.APPLIED_RUNE)?.let { (rune, level) -> "$rune:$level" }.let { it ?: UNKNOWN }
                .let(SkyOceanItemId::rune)
        }

        "PET" -> {
            this.getData(DataTypes.PET_DATA)?.let { (id, _, _, rarity) -> "$id:${rarity.name}" }.let { it ?: UNKNOWN }
                .let(SkyOceanItemId::pet)
        }

        "ENCHANTED_BOOK" -> {
            this.getData(DataTypes.ENCHANTMENTS)?.entries?.firstOrNull()?.let { (key, value) -> "$key:$value" }
                .let { it ?: UNKNOWN }
                .let(SkyOceanItemId::enchantment)
        }

        "ATTRIBUTE_SHARD" -> {
            this.getData(DataTypes.ATTRIBUTES)?.entries?.firstOrNull()?.let { (key, value) -> "$key:$value" }
                .let { it ?: UNKNOWN }.let(SkyOceanItemId::attribute)
        }

        else -> (data ?: UNKNOWN).let(SkyOceanItemId::item)
    }
}

private fun getAppliedRune(tag: CompoundTag): Pair<String, Int>? {
    return tag.getCompoundOrEmpty("runes")?.let { tag ->
        buildMap { tag.keySet().forEach { key -> this[key] = tag.getIntOr(key, 0) } }
    }?.entries?.firstOrNull()?.toPair()
}
