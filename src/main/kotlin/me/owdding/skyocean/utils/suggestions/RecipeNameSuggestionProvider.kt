package me.owdding.skyocean.utils.suggestions

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.util.concurrent.CompletableFuture


object RecipeNameSuggestionProvider : SkyOceanSuggestionProvider {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource?>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions?>? {
        SimpleRecipeApi.recipes.forEach { recipe ->
            recipe.output?.let {
                val name = it.itemName.stripped.takeUnless { name -> name == "Unknown item: ${it.id}" } ?: return@let
                suggest(builder, name)
                if (name.startsWith("[Lvl ")) {
                    suggest(builder, name.substringAfter("]"))
                }
            }
        }
        return builder.buildFuture()
    }
}
