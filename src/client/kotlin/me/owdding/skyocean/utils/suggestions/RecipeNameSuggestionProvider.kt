package me.owdding.skyocean.utils.suggestions

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.commands.SkyOceanSuggestionProvider
import me.owdding.skyocean.features.recipe.crafthelper.SimpleRecipeApi
import me.owdding.skyocean.features.recipe.crafthelper.visitors.RecipeVisitor
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.util.concurrent.CompletableFuture


object RecipeNameSuggestionProvider : SkyOceanSuggestionProvider {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource?>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions?>? {
        SimpleRecipeApi.recipes.forEach { recipe ->
            RecipeVisitor.getOutput(recipe)?.let {
                suggest(builder, it.itemName.stripped)
            }
        }
        return builder.buildFuture()
    }
}
