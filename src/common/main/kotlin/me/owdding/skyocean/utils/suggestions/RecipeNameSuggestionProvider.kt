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
                suggest(builder, it.itemName.stripped)
                if (it.itemName.stripped.startsWith("[Lvl ")) {
                    suggest(builder, it.itemName.stripped.substringAfter("]"))
                }
            }
        }
        return builder.buildFuture()
    }
}
