package me.owdding.skyocean.features.recipe.crafthelper.data

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.resolver.CustomTreeResolver

@GenerateCodec
data class CustomCraftHelperTree(
    var output: ItemLikeIngredient,
    var inputs: MutableMap<String, Int>,
    override var amount: Int = 1,
) : CraftHelperRecipe(CraftHelperRecipeType.CUSTOM, true), CraftHelperRecipe.Amount {

    override fun resolve(
        resetLayout: () -> Unit,
        clear: () -> Unit,
    ): Pair<ContextAwareRecipeTree, ItemLikeIngredient> = CustomTreeResolver.resolve(this, resetLayout, clear)

    override fun withAmount(amount: Int): CraftHelperRecipe = copy(amount = amount)
}
