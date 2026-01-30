package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType

abstract class CraftHelperRecipe(val type: CraftHelperRecipeType, val canModifyCount: Boolean) {
    abstract val hiddenPaths: MutableSet<String>
    abstract fun resolve(resetLayout: () -> Unit, clear: () -> Unit): Pair<ContextAwareRecipeTree, ItemLikeIngredient>?
}

object NoOpCraftHelperRecipe : CraftHelperRecipe(
    CraftHelperRecipeType.NORMAL,
    false
) {
    override val hiddenPaths: MutableSet<String> = mutableSetOf()

    override fun resolve(
        resetLayout: () -> Unit,
        clear: () -> Unit,
    ): Pair<ContextAwareRecipeTree, ItemLikeIngredient>? = null
}
