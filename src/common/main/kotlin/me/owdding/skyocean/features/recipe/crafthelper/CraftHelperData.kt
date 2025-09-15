package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.generated.DispatchHelper
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import kotlin.reflect.KClass

abstract class CraftHelperRecipe(val type: CraftHelperRecipeType)

@GenerateCodec
data class NormalCraftHelperRecipe(
    var item: SkyBlockId?,
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
