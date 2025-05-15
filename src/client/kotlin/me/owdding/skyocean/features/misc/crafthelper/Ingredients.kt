package me.owdding.skyocean.features.misc.crafthelper

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.repolib.api.recipes.ingredient.CraftingIngredient
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient as RepoItemIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.PetIngredient as RepoPetIngredient

interface Ingredient

interface ItemLikeIngredient : Ingredient {
    val skyblockId: String
    val item: ItemStack
    val itemName: Component
    val amount: Int

    fun getRecipe() = SimpleRecipeApi.getBestRecipe(skyblockId)
}

data class ItemIngredient(override val skyblockId: String, override val amount: Int) : ItemLikeIngredient {

    override val item by lazy { RepoItemsAPI.getItem(skyblockId) }
    override val itemName by lazy { RepoItemsAPI.getItemName(skyblockId) }

}

data class PetIngredient(override val skyblockId: String, val tier: String) : ItemLikeIngredient {

    val rarity = runCatching { SkyBlockRarity.valueOf(tier) }.getOrElse { _ -> SkyBlockRarity.COMMON }

    override val item by lazy { RepoPetsAPI.getPetAsItem(skyblockId, rarity) }
    override val itemName: Component by lazy { item.hoverName }
    override val amount = 1

}

data class CoinIngredient(val amount: Int) : Ingredient

fun CraftingIngredient.toSkyOceanIngredient(): Ingredient? {
    return when (this) {
        is RepoItemIngredient -> ItemIngredient(this.id, this.count())
        is RepoPetIngredient -> PetIngredient(this.id, this.tier)
        else -> null
    }
}
