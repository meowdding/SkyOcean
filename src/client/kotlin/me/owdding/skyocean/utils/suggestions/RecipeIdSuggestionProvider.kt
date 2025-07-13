package me.owdding.skyocean.utils.suggestions

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.util.concurrent.CompletableFuture

object RecipeIdSuggestionProvider : SkyOceanSuggestionProvider {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource?>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions?>? {
        SimpleRecipeApi.recipes.forEach { recipe ->
            recipe.output?.let {
                suggest(builder, it.skyblockId)
            }
        }
        return builder.buildFuture()
    }
}

