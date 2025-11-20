package me.owdding.skyocean.features.recipe.crafthelper.resolver

import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.SimpleRecipeApi.getBestRecipe
import me.owdding.skyocean.features.recipe.crafthelper.ContextAwareRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.data.CraftHelperRecipeType
import me.owdding.skyocean.features.recipe.crafthelper.data.NormalCraftHelperRecipe
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object DefaultTreeResolver : TreeResolver<NormalCraftHelperRecipe> {
    override val type: CraftHelperRecipeType get() = CraftHelperRecipeType.NORMAL

    override fun resolve(recipe: NormalCraftHelperRecipe, resetLayout: () -> Unit, clear: () -> Unit): Pair<ContextAwareRecipeTree, ItemLikeIngredient>? {

        val currentRecipe = CraftHelperStorage.selectedItem ?: run {
            resetLayout()
            return null
        }

        val recipe = getBestRecipe(currentRecipe) ?: run {
            Text.of("No recipe found for $currentRecipe!") { this.color = TextColor.RED }.sendWithPrefix()
            resetLayout()
            clear()
            return null
        }
        val output = recipe.output ?: run {
            Text.of("Recipe output is null!") { this.color = TextColor.RED }.sendWithPrefix()
            resetLayout()
            clear()
            return null
        }

        return ContextAwareRecipeTree(recipe, output, CraftHelperStorage.selectedAmount.coerceAtLeast(1)) to output
    }
}
