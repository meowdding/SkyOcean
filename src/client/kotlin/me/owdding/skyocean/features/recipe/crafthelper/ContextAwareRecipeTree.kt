package me.owdding.skyocean.features.recipe.crafthelper

import net.minecraft.world.item.crafting.Recipe

class ContextAwareRecipeTree(val root: Recipe<*>) {
}

@JvmInline
value class RecipeEvaluationContext(val map: Map<String, Int>)
