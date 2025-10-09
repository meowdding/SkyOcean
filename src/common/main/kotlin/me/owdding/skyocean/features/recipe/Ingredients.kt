package me.owdding.skyocean.features.recipe

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.extensions.sanitizeNeu
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.id
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
    ITEM(SkyOceanItemIngredient::class),
    CURRENCY(CurrencyIngredient::class),
    ;

    companion object {
        fun getType(id: String) = IngredientType.entries.firstOrNull { it.id.equals(id, true) } ?: ITEM

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
    val id: SkyBlockId
    val item: ItemStack
    val itemName: Component

    fun getRecipe() = SimpleRecipeApi.getBestRecipe(id)
}

@GenerateCodec
data class SkyOceanItemIngredient(override val id: SkyBlockId, override val amount: Int = 1) : ItemLikeIngredient {
    override val skyblockId: String = id.skyblockId
    override val item: ItemStack by lazy { id.toItem() }
    override val itemName: Component by lazy { item.hoverName }
    override val type: IngredientType = IngredientType.ITEM

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
    is ItemLikeIngredient -> this.id.id
    is CurrencyIngredient -> "ocean:${currency.name.lowercase()}"
    else -> throw UnsupportedOperationException("Can't serialize $this")
}

fun Ingredient.serializeWithAmount(): String = "${serialize()}:$amount"
fun Iterable<Ingredient>.mergeSameTypes(): Iterable<Ingredient> = this.groupBy { it.serialize() }
    .mapValues { (_, ingredient) -> ingredient.first().withAmount(ingredient.sumOf { it.amount }) }
    .values


fun CraftingIngredient.toSkyOceanIngredient(): Ingredient? {
    val id = this.id()?.sanitizeNeu() ?: return null

    return when (this) {
        is RepoItemIngredient -> SkyOceanItemIngredient(SkyBlockId.item(id), this.count())
        is RepoPetIngredient -> SkyOceanItemIngredient(SkyBlockId.pet(id, this.tier()), this.count())
        is RepoEnchantmentIngredient -> SkyOceanItemIngredient(SkyBlockId.enchantment(id, this.level()), this.count())
        is RepoAttributeIngredient -> SkyOceanItemIngredient(SkyBlockId.attribute(id), this.count())
        else -> null
    }
}
