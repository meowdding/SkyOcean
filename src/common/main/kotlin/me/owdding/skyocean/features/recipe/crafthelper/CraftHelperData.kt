package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.features.recipe.*
import me.owdding.skyocean.features.recipe.crafthelper.resolver.DefaultTreeResolver
import me.owdding.skyocean.features.recipe.crafthelper.resolver.SkyShardsTreeResolver
import me.owdding.skyocean.generated.DispatchHelper
import me.owdding.skyocean.repo.attributes.SkyShardsAttributeRepoData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import kotlin.reflect.KClass

abstract class CraftHelperRecipe(val type: CraftHelperRecipeType, val canModifyCount: Boolean) {
    abstract fun resolve(resetLayout: () -> Unit, clear: () -> Unit): Pair<ContextAwareRecipeTree, ItemLikeIngredient>?
}

@GenerateCodec
data class NormalCraftHelperRecipe(
    var item: SkyBlockId?,
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
enum class CraftHelperRecipeType(override val type: KClass<out CraftHelperRecipe>) : DispatchHelper<CraftHelperRecipe> {
    NORMAL(NormalCraftHelperRecipe::class),
    SKY_SHARDS(SkyShardsRecipe::class)
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}


abstract class SkyShardsMethod(
    val type: SkyShardsMethodType,
    open val shard: SkyBlockId,
    open val quantity: Int,
) : ParentRecipe() {
    override val recipeType: RecipeType get() = RecipeType.SKY_SHARDS
    abstract fun visitElements(visitor: (SkyShardsMethod) -> Unit)
}

private val unknownId = SkyBlockId.attribute(SkyBlockId.UNKNOWN)

@GenerateCodec
data class SkyShardsRecipeElement(
    override val shard: SkyBlockId,
    override val quantity: Int,
    val craftsExpected: Int,
    val outputQuantity: Int,
    val pureReptile: Int,
    @FieldName("inputs") val _inputs: List<SkyShardsMethod>,
) : SkyShardsMethod(SkyShardsMethodType.RECIPE, shard, quantity) {
    override val output: ItemLikeIngredient = SkyOceanItemIngredient(shard, outputQuantity)
    override val inputs: List<Ingredient> = _inputs.mapNotNull { it.output }
    override fun getRecipe(ingredient: Ingredient): Recipe? =
        _inputs.find { it.shard == (ingredient as? SkyOceanItemIngredient)?.id }?.takeUnless { it is SkyShardsDirectElement }

    override fun visitElements(visitor: (SkyShardsMethod) -> Unit) {
        visitor(this)
        _inputs.forEach {
            it.visitElements(visitor)
        }
    }
}

@GenerateCodec
data class SkyShardsDirectElement(
    override val shard: SkyBlockId,
    override val quantity: Int,
) : SkyShardsMethod(SkyShardsMethodType.DIRECT, shard, quantity) {
    override val inputs: List<Ingredient> = emptyList()
    override val output: ItemLikeIngredient = SkyOceanItemIngredient(shard, SkyShardsAttributeRepoData.data[shard]?.fuseAmount ?: 1)
    override fun getRecipe(ingredient: Ingredient): Recipe? = null
    override fun visitElements(visitor: (SkyShardsMethod) -> Unit) = visitor(this)
}

@GenerateCodec
data class SkyShardsCycleElement(
    override val shard: SkyBlockId,
    override val quantity: Int,
    val craftsExpected: Int,
    val outputQuantity: Int,
    val pureReptile: Int,
    val steps: List<SkyShardsCycleStep>,
) : SkyShardsMethod(SkyShardsMethodType.CYCLE, shard, quantity) {
    override val inputs: List<Ingredient> = emptyList()
    override val output: ItemLikeIngredient = SkyOceanItemIngredient(unknownId, -1)
    override fun getRecipe(ingredient: Ingredient): Recipe? = null
    override fun visitElements(visitor: (SkyShardsMethod) -> Unit) = visitor(this)
}

@GenerateCodec
data class SkyShardsCycleStep(
    val shard: SkyBlockId,
    val inputs: List<SkyBlockId>,
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
