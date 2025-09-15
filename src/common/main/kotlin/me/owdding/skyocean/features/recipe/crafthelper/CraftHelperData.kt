package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.features.recipe.*
import me.owdding.skyocean.features.recipe.crafthelper.resolver.DefaultTreeResolver
import me.owdding.skyocean.features.recipe.crafthelper.resolver.SkyShardsTreeResolver
import me.owdding.skyocean.features.recipe.crafthelper.resolver.TreeResolver
import me.owdding.skyocean.generated.DispatchHelper
import kotlin.reflect.KClass

abstract class CraftHelperRecipe(val type: CraftHelperRecipeType, val canModifyCount: Boolean) {
    abstract fun resolve(resetLayout: () -> Unit, clear: () -> Unit): Pair<ContextAwareRecipeTree, ItemLikeIngredient>?
}

@GenerateCodec
data class NormalCraftHelperRecipe(
    var item: SkyOceanItemId?,
    var amount: Int = 1,
) : CraftHelperRecipe(CraftHelperRecipeType.NORMAL, true) {
    override fun resolve(
        resetLayout: () -> Unit,
        clear: () -> Unit,
    ): Pair<ContextAwareRecipeTree, ItemLikeIngredient>? {
        return DefaultTreeResolver.resolve(this, resetLayout, clear)
    }
}

@GenerateCodec
data class SkyShardsRecipe(
    var tree: SkyShardsMethod,
) : CraftHelperRecipe(CraftHelperRecipeType.SKY_SHARDS, false) {
    override fun resolve(
        resetLayout: () -> Unit,
        clear: () -> Unit,
    ): Pair<ContextAwareRecipeTree, ItemLikeIngredient>? {
        return SkyShardsTreeResolver.resolve(this, resetLayout, clear)
    }
}

@GenerateDispatchCodec(CraftHelperRecipe::class)
enum class CraftHelperRecipeType(override val type: KClass<out CraftHelperRecipe>, val resolver: TreeResolver<out CraftHelperRecipe>) :
    DispatchHelper<CraftHelperRecipe> {
    NORMAL(NormalCraftHelperRecipe::class, DefaultTreeResolver),
    SKY_SHARDS(SkyShardsRecipe::class, SkyShardsTreeResolver)
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}


abstract class SkyShardsMethod(
    val type: SkyShardsMethodType,
    open val shard: SkyOceanItemId,
    open val quantity: Int,
) : Recipe() {
    override val recipeType: RecipeType get() = RecipeType.SKY_SHARDS
}

@GenerateCodec
data class SkyShardsRecipeElement(
    override val shard: SkyOceanItemId,
    override val quantity: Int,
    val craftsExpected: Int,
    val outputQuantity: Int,
    val pureReptile: Int,
    @FieldName("inputs") val _inputs: List<SkyShardsMethod>,
) : SkyShardsMethod(SkyShardsMethodType.RECIPE, shard, quantity) {
    override val output: ItemLikeIngredient = SkyOceanItemIngredient(shard, quantity)
    override val inputs: List<Ingredient> = _inputs.mapNotNull { it.output }
}

@GenerateCodec
data class SkyShardsDirectElement(
    override val shard: SkyOceanItemId,
    override val quantity: Int,
) : SkyShardsMethod(SkyShardsMethodType.DIRECT, shard, quantity) {
    override val inputs: List<Ingredient> = emptyList()
    override val output: ItemLikeIngredient = SkyOceanItemIngredient(shard, quantity)

}

@GenerateCodec
data class SkyShardsCycleElement(
    override val shard: SkyOceanItemId,
    override val quantity: Int,
    val craftsExpected: Int,
    val outputQuantity: Int,
    val pureReptile: Int,
    val steps: List<SkyShardsCycleStep>,
) : SkyShardsMethod(SkyShardsMethodType.CYCLE, shard, quantity) {
    override val inputs: List<Ingredient> = steps.map { SkyOceanItemIngredient(shard, 1) }
    override val output: ItemLikeIngredient = SkyOceanItemIngredient(shard, quantity)
}

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
