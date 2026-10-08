package me.owdding.skyocean.features.recipe.crafthelper.data

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.resolver.DefaultTreeResolver
import tech.thatgravyboat.skyblockapi.api.remote.api.SimpleItemAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

@GenerateCodec
data class NormalCraftHelperRecipe(
    @FieldName("item") override var selectedItem: SkyBlockId?,
    override var amount: Int = 1,
) : CraftHelperRecipe(CraftHelperRecipeType.NORMAL), CraftHelperRecipe.MutableCount, CraftHelperRecipe.Ingredients {

    override fun resolve(
        resetLayout: () -> Unit,
        clear: () -> Unit,
    ): CraftHelperTree? {
        return DefaultTreeResolver.resolve(this, resetLayout, clear)
    }

    override fun withAmount(amount: Int): CraftHelperRecipe = copy(amount = amount)
    override val inputs: List<Ingredient> get() = listOf(SimpleRecipeApi.getBestRecipe(selectedItem ?: return emptyList())?.output ?: return emptyList())
    override val output: ItemLikeIngredient? get() = SimpleRecipeApi.getBestRecipe(selectedItem ?: return null)?.output
}
