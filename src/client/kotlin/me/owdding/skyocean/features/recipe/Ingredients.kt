package me.owdding.skyocean.features.recipe

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.api.SimpleRecipeApi
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.reflect.KClass
import tech.thatgravyboat.repolib.api.recipes.ingredient.AttributeIngredient as RepoAttributeIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.EnchantmentIngredient as RepoEnchantmentIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient as RepoItemIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.PetIngredient as RepoPetIngredient

@GenerateDispatchCodec(Ingredient::class)
enum class IngredientType(override val type: KClass<out Ingredient>) : me.owdding.skyocean.generated.DispatchHelper<Ingredient> {
    ITEM(ItemIngredient::class),
    PET(PetIngredient::class),
    ENCHANTMENT(EnchantmentIngredient::class),
    ATTRIBUTE(AttributeIngredient::class),
    CURRENCY(CurrencyIngredient::class),
    ;

    companion object {
        fun getType(id: String) = IngredientType.entries.first { it.id.equals(id, true) }

        @IncludedCodec
        val ITEM_LIKE: Codec<ItemLikeIngredient> = SkyOceanCodecs.IngredientCodec.codec().xmap({ it as ItemLikeIngredient }, { it })
    }
}

interface Ingredient {
    val amount: Int
    val type: IngredientType

    fun withAmount(amount: Int): Ingredient
    operator fun plus(other: Ingredient): Ingredient {
        return this.withAmount(this.amount + other.amount)
    }
}

interface ItemLikeIngredient : Ingredient {
    val skyblockId: String
    val item: ItemStack
    val itemName: Component

    fun getRecipe() = SimpleRecipeApi.getBestRecipe(skyblockId)
}

@GenerateCodec
data class ItemIngredient(override val skyblockId: String, override val amount: Int) : ItemLikeIngredient {

    override val item by lazy { RepoItemsAPI.getItem(skyblockId) }
    override val itemName by lazy { RepoItemsAPI.getItemName(skyblockId) }
    override val type: IngredientType = IngredientType.ITEM

    override fun withAmount(amount: Int): Ingredient {
        return ItemIngredient(skyblockId, amount)
    }

}

@GenerateCodec
data class PetIngredient(override val skyblockId: String, val tier: String, override val amount: Int = 1) :
    ItemLikeIngredient {

    val rarity = runCatching { SkyBlockRarity.valueOf(tier) }.getOrElse { _ -> SkyBlockRarity.COMMON }

    override val item by lazy { RepoPetsAPI.getPetAsItem(skyblockId, rarity) }
    override val itemName: Component by lazy { item.hoverName }
    override val type: IngredientType = IngredientType.PET

    override fun withAmount(amount: Int) = copy(amount = amount)
}

@GenerateCodec
data class EnchantmentIngredient(override val skyblockId: String, val level: Int, override val amount: Int) : ItemLikeIngredient {
    override val item: ItemStack by lazy { ItemStack.EMPTY } //TODO
    override val itemName: Component = Text.of(skyblockId)
    override val type: IngredientType = IngredientType.ENCHANTMENT

    override fun withAmount(amount: Int) = copy(amount = amount)
}

@GenerateCodec
data class AttributeIngredient(override val skyblockId: String, override val amount: Int) : ItemLikeIngredient {
    override val item: ItemStack by lazy { ItemStack.EMPTY } //TODO
    override val itemName: Component = Text.of(skyblockId)
    override val type: IngredientType = IngredientType.ATTRIBUTE

    override fun withAmount(amount: Int) = copy(amount = amount)
}

enum class CurrencyType(val displayName: Component) {
    COIN(Text.of("Coin") { this.color = TextColor.GOLD }),
    BIT(Text.of("Bit") { this.color = TextColor.AQUA }),
    COPPER(Text.of("Copper") { this.color = TextColor.RED }),
    FOSSIL_DUST(Text.of("Fossil Dust") { this.color = TextColor.WHITE }),
    BRONZE_MEDAL(Text.of("Bronze Medal") { this.color = TextColor.RED }),
    SILVER_MEDAL(Text.of("Silver Medal") { this.color = TextColor.WHITE }),
    GOLD_MEDAL(Text.of("Gold Medal") { this.color = TextColor.GOLD }),
    MOTE(Text.of("Motes") { this.color = TextColor.PINK }),
    NORTH_STAR(Text.of("North Stars") { this.color = TextColor.PINK }),
    PELT(Text.of("Pelt") { this.color = TextColor.MAGENTA }),
    GEM(Text.of("Gems") { this.color = TextColor.GREEN }),
    CHOCOLATE(Text.of("Chocolate") { this.color = TextColor.GOLD }),
    ;
}

@GenerateCodec
data class CurrencyIngredient(override val amount: Int, val currency: CurrencyType) : Ingredient {
    override fun withAmount(amount: Int): Ingredient = CurrencyIngredient(amount, currency)
    val displayName: Component get() = currency.displayName
    override val type: IngredientType = IngredientType.CURRENCY

    companion object {
        fun coins(amount: Int) = CurrencyIngredient(amount, CurrencyType.COIN)
    }
}

fun Ingredient.serialize(): String = when (this) {
    is ItemLikeIngredient -> this.skyblockId
    is CurrencyIngredient -> "ocean:${currency.name.lowercase()}"
    else -> throw UnsupportedOperationException("Can't serialize $this")
}

fun Ingredient.serializeWithAmount(): String = "${serialize()}:$amount"
fun Iterable<Ingredient>.mergeSameTypes(): Iterable<Ingredient> = this.groupBy { it.serialize() }
    .mapValues { (_, ingredient) -> ingredient.first().withAmount(ingredient.sumOf { it.amount }) }
    .values

fun CraftingIngredient.toSkyOceanIngredient(): Ingredient? {
    return when (this) {
        is RepoItemIngredient -> ItemIngredient(this.id, this.count())
        is RepoPetIngredient -> PetIngredient(this.id, this.tier, this.count)
        is RepoEnchantmentIngredient -> EnchantmentIngredient(this.id, this.level, this.count)
        is RepoAttributeIngredient -> AttributeIngredient(this.id, this.count)
        else -> null
    }
}
