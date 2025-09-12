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
    open val shard: String,
    open val quantity: Int,
) {
    val shardId by lazy { SkyOceanItemId.attribute(shard) }
}

@GenerateCodec
data class SkyShardsRecipeElement(
    override val shard: String,
    override val quantity: Int,
    val craftsExpected: Int,
    val outputQuantity: Int,
    val pureReptile: Int,
    val inputs: List<SkyShardsMethod>,
) : SkyShardsMethod(SkyShardsMethodType.RECIPE, shard, quantity)

@GenerateCodec
data class SkyShardsDirectElement(
    override val shard: String,
    override val quantity: Int,
) : SkyShardsMethod(SkyShardsMethodType.DIRECT, shard, quantity)

@GenerateDispatchCodec(SkyShardsMethod::class, "method")
enum class SkyShardsMethodType(override val type: KClass<out SkyShardsMethod>) : DispatchHelper<SkyShardsMethod> {
    RECIPE(SkyShardsRecipeElement::class),
    DIRECT(SkyShardsDirectElement::class),
    // TODO what the fuck is cycle
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}
