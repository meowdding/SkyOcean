package me.owdding.skyocean.api

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.api.SkyOceanItemId.Companion.UNKNOWN
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
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

        fun item(id: String) = SkyOceanItemId("$ITEM$id".lowercase())
        fun pet(id: String) = SkyOceanItemId("$PET$id".lowercase())
        fun pet(id: String, rarity: String) = SkyOceanItemId("$PET$id:$rarity".lowercase())
        fun rune(id: String) = SkyOceanItemId("$RUNE$id".lowercase())
        fun attribute(id: String) = SkyOceanItemId("$ATTRIBUTE$id".lowercase())
        fun enchantment(id: String) = SkyOceanItemId("$ENCHANTMENT$id".lowercase())
        fun enchantment(id: String, level: Int) = SkyOceanItemId("$ENCHANTMENT$id:$level".lowercase())

        fun fromItem(item: ItemStack) = item.getSkyOceanItemId()

        fun fromName(name: String): SkyOceanItemId? {
            var name = name.lowercase().stripColor()
            if (name.startsWith("[lvl")) {
                name = name.substringAfterLast("]")
            } else if (name.matches(amountRegex)) {
                name = name.substringBeforeLast(" x")
            }

            return SimpleItemApi.findIdByName(name.trim()) ?: SimpleItemApi.findIdByName(name.substringBeforeLast(" ").trim())
        }

        fun unknownType(input: String): SkyOceanItemId? {
            val unsafeId = SkyOceanItemId(input)

            SimpleItemApi.getItemByIdOrNull(unsafeId) ?: return item(input)
            SimpleItemApi.getPetByIdOrNull(unsafeId) ?: return pet(input)
            SimpleItemApi.getEnchantmentByIdOrNull(unsafeId) ?: return enchantment(input)
            SimpleItemApi.getAttributeByIdOrNull(unsafeId) ?: return attribute(input)
            SimpleItemApi.getRuneByIdOrNull(unsafeId) ?: return rune(input)

            return null
        }

        fun unsafe(id: String) = SkyOceanItemId(id)

        @IncludedCodec
        val CODEC: Codec<SkyOceanItemId> = Codec.STRING.xmap(::SkyOceanItemId, SkyOceanItemId::id)

        fun ItemStack.getSkyOceanId() = fromItem(this) ?: fromName(this.hoverName.stripped)

    }

    val isItem: Boolean get() = id.startsWith(ITEM)
    val isPet: Boolean get() = id.startsWith(PET)
    val isRune: Boolean get() = id.startsWith(RUNE)
    val isEnchantment: Boolean get() = id.startsWith(ENCHANTMENT)
    val isAttribute: Boolean get() = id.startsWith(ATTRIBUTE)
    val cleanId: String get() = id.substringAfter(":")

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
            this.getData(DataTypes.ATTRIBUTES)?.entries?.firstOrNull()?.let { (key, _) -> key }
                .let { it ?: UNKNOWN }.let(SkyOceanItemId::attribute)
        }

        else -> (data)?.let(SkyOceanItemId::item)
    }
}
