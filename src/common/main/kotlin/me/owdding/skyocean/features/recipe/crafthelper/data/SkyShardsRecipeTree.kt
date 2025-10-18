package me.owdding.skyocean.features.recipe.crafthelper.data

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.GenerateDispatchCodec
import me.owdding.skyocean.features.recipe.*
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.resolver.SkyShardsTreeResolver
import me.owdding.skyocean.generated.DispatchHelper
import me.owdding.skyocean.repo.attributes.SkyShardsAttributeRepoData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import kotlin.reflect.KClass


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

abstract class SkyShardsMethod(
    val type: SkyShardsMethodType,
    open val shard: SkyBlockId,
    open val quantity: Int,
) : ParentRecipe {
    override val recipeType: RecipeType get() = RecipeType.SKY_SHARDS
    abstract fun visitElements(visitor: (SkyShardsMethod) -> Unit)
    abstract fun <T> visitElements(parent: T?, visitor: (parent: T?, self: SkyShardsMethod) -> T): T
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

    override fun <T> visitElements(parent: T?, visitor: (parent: T?, self: SkyShardsMethod) -> T): T {
        val self = visitor(parent, this)
        _inputs.forEach {
            it.visitElements(self, visitor)
        }
        return self
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
    override fun <T> visitElements(parent: T?, visitor: (parent: T?, self: SkyShardsMethod) -> T) = visitor(parent, this)
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
    override fun <T> visitElements(parent: T?, visitor: (parent: T?, self: SkyShardsMethod) -> T) = visitor(parent, this)
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
    CYCLE(SkyShardsCycleElement::class),
    ;

    companion object {
        fun getType(id: String) = valueOf(id.uppercase())
    }
}
