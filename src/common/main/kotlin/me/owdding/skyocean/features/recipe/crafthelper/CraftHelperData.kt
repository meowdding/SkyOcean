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

@GenerateCodec
data class SkyShardsRecipe(
    var tree: SkyShardsMethod,
) : CraftHelperRecipe(CraftHelperRecipeType.SKYSHARDS)

@GenerateDispatchCodec(CraftHelperRecipe::class)
enum class CraftHelperRecipeType(override val type: KClass<out CraftHelperRecipe>) : DispatchHelper<CraftHelperRecipe> {
    NORMAL(NormalCraftHelperRecipe::class),
    SKYSHARDS(SkyShardsRecipe::class)
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}


abstract class SkyShardsMethod(
    val type: SkyShardsMethodType,
    open val shard: SkyOceanItemId,
    open val quantity: Int,
)

@GenerateCodec
data class SkyShardsRecipeElement(
    override val shard: SkyOceanItemId,
    override val quantity: Int,
    val craftsExpected: Int,
    val outputQuantity: Int,
    val pureReptile: Int,
    val inputs: List<SkyShardsMethod>,
) : SkyShardsMethod(SkyShardsMethodType.RECIPE, shard, quantity)

@GenerateCodec
data class SkyShardsDirectElement(
    override val shard: SkyOceanItemId,
    override val quantity: Int,
) : SkyShardsMethod(SkyShardsMethodType.DIRECT, shard, quantity)

@GenerateCodec
data class SkyShardsCycleElement(
    override val shard: SkyOceanItemId,
    override val quantity: Int,
    val craftsExpected: Int,
    val outputQuantity: Int,
    val pureReptile: Int,
    val steps: List<SkyShardsCycleStep>,
) : SkyShardsMethod(SkyShardsMethodType.CYCLE, shard, quantity)

@GenerateCodec
data class SkyShardsCycleStep(
    val shard: SkyOceanItemId,
    val inputs: List<SkyOceanItemId>,
)

@GenerateDispatchCodec(SkyShardsMethod::class, "method")
enum class SkyShardsMethodType(override val type: KClass<out SkyShardsMethod>) : DispatchHelper<SkyShardsMethod> {
    RECIPE(SkyShardsRecipeElement::class),
    DIRECT(SkyShardsDirectElement::class),
    CYCLE(SkyShardsCycleElement::class)
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}
