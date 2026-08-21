package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.data.CustomCraftHelperTree
import me.owdding.skyocean.features.recipe.custom.CustomRecipe
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

object CustomTreeResolver : TreeResolver<CustomCraftHelperTree> {
    override val type: CraftHelperRecipeType get() = CraftHelperRecipeType.CUSTOM

    override fun resolve(
        recipe: CustomCraftHelperTree,
        resetLayout: () -> Unit,
        clear: () -> Unit,
    ): CraftHelperTree {
        val customRecipe = CustomRecipe(
            recipe.output,
            recipe.inputs.map { (id, amount) ->
                SkyOceanItemIngredient(
                    SkyBlockId.unknownType(id.split(":").drop(1).joinToString("")) ?: SkyBlockId.EMPTY,
                    amount * recipe.output.amount,
                )
            }.toMutableList(),
        )

        val output = recipe.output

        return CraftHelperTree(customRecipe, output, CraftHelperStorage.selectedAmount.coerceAtLeast(1))
    }
}
