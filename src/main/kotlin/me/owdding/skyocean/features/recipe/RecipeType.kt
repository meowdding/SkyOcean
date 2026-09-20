package me.owdding.skyocean.features.recipe

import tech.thatgravyboat.repolib.api.recipes.Recipe

enum class RecipeType(val command: String? = null, val repoLibType: Recipe.Type<*>? = null) {
    CRAFTING("viewrecipe", Recipe.Type.CRAFTING),
    FORGE("viewforgerecipe", Recipe.Type.FORGE),
    KAT(repoLibType = Recipe.Type.KAT),
    SHOP(repoLibType = Recipe.Type.SHOP),
    CUSTOM,
    SKY_SHARDS,
    UNKNOWN,
    ;

    companion object {
        fun fromRepoLibType(type: Recipe.Type<*>) = entries.find { it.repoLibType == type }
    }
}
