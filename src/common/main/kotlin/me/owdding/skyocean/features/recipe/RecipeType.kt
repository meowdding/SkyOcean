package me.owdding.skyocean.features.recipe


enum class RecipeType(val command: String? = null) {
    CRAFTING("viewrecipe"),
    FORGE("viewforgerecipe"),
    KAT,
    CUSTOM,
    SKY_SHARDS,
    UNKNOWN,
}
