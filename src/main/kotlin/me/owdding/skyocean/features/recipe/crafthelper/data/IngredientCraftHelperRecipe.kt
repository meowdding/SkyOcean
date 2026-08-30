package me.owdding.skyocean.features.recipe.crafthelper.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.features.recipe.CustomRecipeIngredient
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.Recipe
import me.owdding.skyocean.features.recipe.RecipeType
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.resolver.IngredientTreeResolver
import me.owdding.skyocean.features.recipe.mergeSameTypes
import me.owdding.skyocean.utils.extensions.replaceValues
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

@GenerateCodec
data class IngredientCraftHelperRecipe(override val inputs: MutableList<Ingredient> = mutableListOf(), override var amount: Int = 1) :
    CraftHelperRecipe(CraftHelperRecipeType.INGREDIENT_RECIPE),
    Recipe,
    CraftHelperRecipe.MutableCount,
    CraftHelperRecipe.Ingredients {

    override val selectedItem: SkyBlockId? = null
    override fun resolve(resetLayout: () -> Unit, clear: () -> Unit): CraftHelperTree? {
        return IngredientTreeResolver.resolve(this, resetLayout, clear)
    }

    override val output: ItemLikeIngredient = CustomRecipeIngredient(this)
    override val recipeType: RecipeType = RecipeType.UNKNOWN
    override fun withAmount(amount: Int): CraftHelperRecipe = copy(amount = amount)

    fun add(iterable: Iterable<Ingredient>) {
        inputs.addAll(iterable)
        inputs.replaceValues(inputs.mergeSameTypes())
    }
    fun add(iterable: Ingredient) {
        inputs.add(iterable)
        inputs.replaceValues(inputs.mergeSameTypes())
    }
}
