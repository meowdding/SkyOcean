package me.owdding.skyocean.features.recipe.crafthelper

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient as RepoItemIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.PetIngredient as RepoPetIngredient

interface Ingredient {
    val amount: Int

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

data class ItemIngredient(override val skyblockId: String, override val amount: Int) : ItemLikeIngredient {

    override val item by lazy { RepoItemsAPI.getItem(skyblockId) }
    override val itemName by lazy { RepoItemsAPI.getItemName(skyblockId) }
    override fun withAmount(amount: Int): Ingredient {
        return ItemIngredient(skyblockId, amount)
    }

}

data class PetIngredient(override val skyblockId: String, val tier: String, override val amount: Int = 1) : ItemLikeIngredient {

    val rarity = runCatching { SkyBlockRarity.valueOf(tier) }.getOrElse { _ -> SkyBlockRarity.COMMON }

    override val item by lazy { RepoPetsAPI.getPetAsItem(skyblockId, rarity) }
    override val itemName: Component by lazy { item.hoverName }
    override fun withAmount(amount: Int): Ingredient {
        return PetIngredient(skyblockId, tier, amount)
    }

}

data class CoinIngredient(override val amount: Int) : Ingredient {
    override fun withAmount(amount: Int) = CoinIngredient(amount)
}

fun Ingredient.serialize(): String = when (this) {
    is ItemLikeIngredient -> this.skyblockId
    is CoinIngredient -> "ocean:coins"
    else -> throw UnsupportedOperationException("Can't serialize $this")
}

fun CraftingIngredient.toSkyOceanIngredient(): Ingredient? {
    return when (this) {
        is RepoItemIngredient -> ItemIngredient(this.id, this.count())
        is RepoPetIngredient -> PetIngredient(this.id, this.tier)
        else -> null
    }
}
