package me.owdding.skyocean.features.recipe


enum class RecipeType(val command: String) {
    CRAFTING("viewrecipe"),
    FORGE("viewforgerecipe"),
    CUSTOM(""),
    UNKNOWN(""),
    ;
}
