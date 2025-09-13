package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.generated.DispatchHelper
import kotlin.reflect.KClass

abstract class CraftHelperRecipe(val type: CraftHelperRecipeType)

@GenerateCodec
data class NormalCraftHelperRecipe(
    var item: SkyOceanItemId?,
    var amount: Int = 1,
) : CraftHelperRecipe(CraftHelperRecipeType.NORMAL)

@GenerateDispatchCodec(CraftHelperRecipe::class)
enum class CraftHelperRecipeType(override val type: KClass<out CraftHelperRecipe>) : DispatchHelper<CraftHelperRecipe> {
    NORMAL(NormalCraftHelperRecipe::class),
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}
