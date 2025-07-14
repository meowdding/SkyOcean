package me.owdding.skyocean.features.recipe

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.utils.LateInitModule
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import me.owdding.skyocean.features.recipe.RepoApiRecipe as RepoApiRecipeWrapper
import tech.thatgravyboat.repolib.api.recipes.Recipe as RepoApiRecipe

private val illegalIngredients = listOf(
    "DIAMOND_BLOCK",
    "IRON_BLOCK",
    "EMERALD_BLOCK",
    "COAL_BLOCK",
    "REDSTONE_BLOCK",
    "GOLD_BLOCK",
    "LAPIS_BLOCK",
    "HAY_BLOCK",
    "SLIME_BLOCK",
)

@LateInitModule
object SimpleRecipeApi {

    internal val supportedTypes = arrayOf(
        RepoApiRecipe.Type.FORGE to RecipeType.FORGE,
        RepoApiRecipe.Type.CRAFTING to RecipeType.CRAFTING
    )

    internal val recipes = mutableListOf<Recipe>()
    internal val idToRecipes: MutableMap<SkyOceanItemId, List<Recipe>> = mutableMapOf()

    init {
        supportedTypes.forEach { (recipe, type) ->
            recipes += RepoAPI.recipes().getRecipes(recipe).map { recipe ->
                RepoApiRecipeWrapper(
                    recipe,
                    type
                )
            }
        }
        recipes.removeIf {
            isBlacklisted(it).apply {
                if (this) {
                    SkyOcean.debug(
                        "Removing ${it.output?.skyblockId} with ${it.inputs.size} ingredients",
                    )
                }
            }
        }
        SkyOcean.trace("Loaded ${recipes.size} Recipes from repo api")

        rebuildRecipes()

        McClient.runNextTick {
            val amount = recipes.flatMap {
                buildList {
                    add(it.output)
                    addAll(it.inputs)
                }.filterIsInstance<ItemLikeIngredient>()
            }.distinct().onEach { it.itemName }.count()
            SkyOcean.trace("Preloaded $amount items")
        }
    }

    fun rebuildRecipes() {
        idToRecipes.clear()
        idToRecipes.putAll(
            recipes.mapNotNull { recipe -> recipe.output?.id?.let { recipe to it } }
                .groupBy { it.second }
                .mapValues { (_, v) -> v.map { it.first } }
                .filter { (_, v) -> v.isNotEmpty() },
        )
    }

    fun hasRecipe(id: SkyOceanItemId) = idToRecipes.containsKey(id)

    fun getBestRecipe(ingredient: Ingredient) =
        (ingredient as? ItemLikeIngredient)?.id?.takeIf(::hasRecipe)?.let { getBestRecipe(it) }

    fun getBestRecipe(id: SkyOceanItemId): Recipe? {
        assert(hasRecipe(id)) { "Item has no recipe" }
        return runCatching {
            idToRecipes[id]!!.firstOrNull()!!
        }.getOrNull()
    }

    fun isBlacklisted(recipe: Recipe): Boolean {
        val inputs = recipe.inputs
        val itemInputs = inputs.filterIsInstance<ItemLikeIngredient>().map { it.skyblockId }.distinct()
        if (inputs.size == itemInputs.size && itemInputs.size == 1 && illegalIngredients.containsAll(itemInputs)) {
            return true
        }

        return illegalIngredients.contains(recipe.output?.skyblockId)
    }

    fun <K> MutableMap<K, Ingredient>.addOrPut(key: K, ingredient: Ingredient): Ingredient =
        merge(key, ingredient) { a, b -> a + b }!!
}

