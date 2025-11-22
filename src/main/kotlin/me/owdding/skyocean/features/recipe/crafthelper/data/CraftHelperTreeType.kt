package me.owdding.skyocean.features.recipe.crafthelper.data

import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.generated.DispatchHelper
import kotlin.reflect.KClass


@GenerateDispatchCodec(CraftHelperRecipe::class)
enum class CraftHelperRecipeType(override val type: KClass<out CraftHelperRecipe>) : DispatchHelper<CraftHelperRecipe> {
    NORMAL(NormalCraftHelperRecipe::class),
    SKY_SHARDS(SkyShardsRecipe::class),
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}
