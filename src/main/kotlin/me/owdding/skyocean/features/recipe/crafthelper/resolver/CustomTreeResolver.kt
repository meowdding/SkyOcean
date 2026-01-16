package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.data.Meow
import me.owdding.skyocean.features.recipe.custom.CustomRecipe
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

object CustomTreeResolver : TreeResolver<Meow> {
    override val type: CraftHelperRecipeType get() = CraftHelperRecipeType.CUSTOM

    override fun resolve(
        meow: Meow,
        resetLayout: () -> Unit,
        clear: () -> Unit,
    ): Pair<ContextAwareRecipeTree, ItemLikeIngredient> {
        val recipe = CustomRecipe(
            meow.output,
            meow.inputs.map { (id, amount) ->
                SkyOceanItemIngredient(
                    SkyBlockId.unknownType(id.split(":").drop(1).joinToString("")) ?: SkyBlockId.EMPTY,
                    amount * meow.output.amount
                )
            }.toMutableList()
        )

        val output = meow.output

        return ContextAwareRecipeTree(recipe, output, CraftHelperStorage.selectedAmount.coerceAtLeast(1)) to output
    }
}
