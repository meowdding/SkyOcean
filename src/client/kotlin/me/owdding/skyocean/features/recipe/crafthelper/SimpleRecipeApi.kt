package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.recipe.crafthelper.visitors.RecipeVisitor
import me.owdding.skyocean.utils.LateInitModule
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.skyblockapi.helpers.McClient

val illegalIngredients = listOf(
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

    internal val supportedTypes = arrayOf(Recipe.Type.FORGE, Recipe.Type.CRAFTING)
    internal val recipes = mutableListOf<Recipe<*>>()
    internal val idToRecipes: Map<String, List<Recipe<*>>>

    init {
        supportedTypes.forEach { recipeType ->
            recipes += RepoAPI.recipes().getRecipes(recipeType)
        }
        recipes.removeIf {
            isBlacklisted(it).apply {
                if (this) {
                    SkyOcean.debug(
                        "Removing ${RecipeVisitor.getOutput(it)?.skyblockId} with ${
                            RecipeVisitor.getInputs(
                                it,
                            ).size
                        } ingredients",
                    )
                }
            }
        }
        SkyOcean.trace("Loaded ${recipes.size} Recipes from repo api")

        idToRecipes = recipes.mapNotNull { recipe -> RecipeVisitor.getOutput(recipe)?.skyblockId?.let { recipe to it } }
            .groupBy { it.second }
            .mapValues { (k, v) -> v.map { it.first } }
            .filter { (_, v) -> v.isNotEmpty() }

        McClient.runNextTick {
            val amount = recipes.flatMap {
                buildList {
                    add(RecipeVisitor.getOutput(it))
                    addAll(RecipeVisitor.getInputs(it))
                }.filterIsInstance<ItemLikeIngredient>()
            }.onEach { it.itemName }.count()
            SkyOcean.trace("Preloaded $amount items")
        }
    }

    fun hasRecipe(id: String) = idToRecipes.containsKey(id)

    fun getBestRecipe(ingredient: Ingredient) =
        (ingredient as? ItemLikeIngredient)?.skyblockId?.takeIf(::hasRecipe)?.let { getBestRecipe(it) }

    fun getBestRecipe(id: String): Recipe<*>? {
        assert(hasRecipe(id)) { "Item has no recipe" }
        runCatching {
            return idToRecipes[id]!!.firstOrNull()!!
        }.getOrElse {
            return null
        }
    }

    fun isBlacklisted(recipe: Recipe<*>): Boolean {
        val inputs = RecipeVisitor.getInputs(recipe)
        val itemInputs = inputs.filterIsInstance<ItemLikeIngredient>().map { it.skyblockId }.distinct()
        if (inputs.size == itemInputs.size && itemInputs.size == 1 && illegalIngredients.containsAll(itemInputs)) {
            return true
        }

        return illegalIngredients.contains(RecipeVisitor.getOutput(recipe)?.skyblockId)
    }

    fun <K> MutableMap<K, Ingredient>.addOrPut(key: K, ingredient: Ingredient): Ingredient =
        merge(key, ingredient) { a, b -> a + b }!!
}

