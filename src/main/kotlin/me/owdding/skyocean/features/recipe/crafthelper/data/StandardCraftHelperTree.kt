package me.owdding.skyocean.features.recipe.crafthelper.data

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.resolver.DefaultTreeResolver
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

@GenerateCodec
data class NormalCraftHelperRecipe(
    @FieldName("item") override var selectedItem: SkyBlockId?,
    override var amount: Int = 1,
) : CraftHelperRecipe(CraftHelperRecipeType.NORMAL), CraftHelperRecipe.MutableCount {

    override fun resolve(
        resetLayout: () -> Unit,
        clear: () -> Unit,
    ): CraftHelperTree? {
        return DefaultTreeResolver.resolve(this, resetLayout, clear)
    }

    override fun withAmount(amount: Int): CraftHelperRecipe = copy(amount = amount)
}
