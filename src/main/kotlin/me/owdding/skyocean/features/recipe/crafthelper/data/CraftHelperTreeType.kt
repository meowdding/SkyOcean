package me.owdding.skyocean.features.recipe.crafthelper.data

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.generated.DispatchHelper
import kotlin.reflect.KClass


@GenerateDispatchCodec(CraftHelperRecipe::class)
enum class CraftHelperRecipeType(override val type: KClass<out CraftHelperRecipe>, val codecOverride: MapCodec<out CraftHelperRecipe>? = null) : DispatchHelper<CraftHelperRecipe> {
    NORMAL(NormalCraftHelperRecipe::class),
    SKY_SHARDS(SkyShardsRecipe::class),
    REPO_LIB_RECIPE(RepoLibRecipeTree::class, RepoLibRecipeTree.CODEC),
    INGREDIENT_RECIPE(IngredientCraftHelperRecipe::class),
    FETCHUR(FetchurCraftHelperRecipe::class),
    ;

    override val codec: MapCodec<out CraftHelperRecipe>
        get() = codecOverride ?: super.codec

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}
