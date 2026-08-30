package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

abstract class CraftHelperRecipe(val type: CraftHelperRecipeType) {
    abstract val amount: Int
    abstract val selectedItem: SkyBlockId?

    interface MutableCount {
        var amount: Int
        fun withAmount(amount: Int = this.amount): CraftHelperRecipe
    }
    interface MultiplesOf {
        val multiples: Int
    }
    interface Ingredients {
        val inputs: List<Ingredient>
        val output: ItemLikeIngredient?

        val entriesForAddition: List<Ingredient> get() = inputs
    }

    abstract fun resolve(resetLayout: () -> Unit, clear: () -> Unit): CraftHelperTree?
}
