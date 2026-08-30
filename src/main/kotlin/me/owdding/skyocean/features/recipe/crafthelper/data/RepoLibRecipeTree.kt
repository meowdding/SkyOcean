package me.owdding.skyocean.features.recipe.crafthelper.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.Inline
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.RepoApiRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.resolver.RepoLibTreeResolver
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

@GenerateCodec
data class RepoLibRecipeTree(
    @Inline var recipe: RepoApiRecipe,
    override var amount: Int,
) : CraftHelperRecipe(CraftHelperRecipeType.REPO_LIB_RECIPE) , CraftHelperRecipe.MutableCount, CraftHelperRecipe.MultiplesOf, CraftHelperRecipe.Ingredients {
    override val selectedItem: SkyBlockId? get() = recipe.output?.id

    override fun resolve(resetLayout: () -> Unit, clear: () -> Unit): CraftHelperTree? {
        return RepoLibTreeResolver.resolve(this, resetLayout, clear)
    }

    override fun withAmount(amount: Int): RepoLibRecipeTree = copy(amount = amount)
    override val multiples: Int get() = recipe.output?.amount ?: 1
    override val inputs: List<Ingredient> = recipe.inputs
    override val output: ItemLikeIngredient? = recipe.output
}
