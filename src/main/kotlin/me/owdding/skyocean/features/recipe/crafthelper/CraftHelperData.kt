package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

abstract class CraftHelperRecipe(val type: CraftHelperRecipeType, val canModifyCount: Boolean) {

    abstract val amount: Int

    abstract fun resolve(resetLayout: () -> Unit, clear: () -> Unit): Pair<ContextAwareRecipeTree, ItemLikeIngredient>?

    interface Amount {
        fun withAmount(amount: Int): CraftHelperRecipe
    }

    interface SkyblockId {
        val resultId: SkyBlockId?
    }
}
