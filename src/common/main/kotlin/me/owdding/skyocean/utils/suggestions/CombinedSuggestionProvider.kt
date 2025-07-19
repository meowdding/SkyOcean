package me.owdding.skyocean.utils.suggestions

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.util.concurrent.CompletableFuture

data class CombinedSuggestionProvider(val providers: List<SkyOceanSuggestionProvider>) : SkyOceanSuggestionProvider {
    constructor(vararg providers: SkyOceanSuggestionProvider) : this(providers.toList())

    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val futures = providers.map { it.getSuggestions(context, SuggestionsBuilder(builder.input, builder.start)) }

        return CompletableFuture.allOf(*futures.toTypedArray()).thenCompose {
            val suggestions = futures.map { it.join() }
            CompletableFuture.completedFuture(Suggestions.merge(builder.input, suggestions))
        }
    }

}
