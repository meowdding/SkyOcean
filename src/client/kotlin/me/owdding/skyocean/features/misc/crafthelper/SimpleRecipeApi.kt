package me.owdding.skyocean.features.misc.crafthelper

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.misc.crafthelper.visitors.RecipeVisitor
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.recipes.Recipe

object SimpleRecipeApi {

    internal val supportedTypes = arrayOf(Recipe.Type.FORGE, Recipe.Type.CRAFTING)
    internal val recipes = mutableListOf<Recipe<*>>()
    internal val idToRecipes: Map<String, List<Recipe<*>>>

    init {
        supportedTypes.forEach { recipeType ->
            recipes += RepoAPI.recipes().getRecipes(recipeType)
        }
        SkyOcean.trace("Loaded ${recipes.size} Recipes from repo api")

        idToRecipes = recipes.mapNotNull { recipe -> RecipeVisitor.getOutput(recipe)?.skyblockId?.let { recipe to it } }
            .groupBy { it.second }
            .mapValues { (k, v) -> v.map { it.first } }
            .filter { (_, v) -> v.isNotEmpty() }
    }

    fun hasRecipe(id: String) = idToRecipes.containsKey(id)

    fun getBestRecipe(id: String): Recipe<*>? {
        assert(hasRecipe(id)) { "Item has no recipe" }
        return idToRecipes[id]?.maxByOrNull {
            1
        }!!
    }
}
