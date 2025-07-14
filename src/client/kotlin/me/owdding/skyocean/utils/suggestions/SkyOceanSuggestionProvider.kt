package me.owdding.skyocean.utils.suggestions

import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.utils.Utils.sanitizeForCommandInput
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider

interface SkyOceanSuggestionProvider : SuggestionProvider<FabricClientCommandSource> {

    fun suggest(builder: SuggestionsBuilder, name: String) {
        val filtered = name.sanitizeForCommandInput()
        if (SharedSuggestionProvider.matchesSubStr(builder.remaining.lowercase(), filtered.lowercase())) {
            builder.suggest(filtered)
        }
    }

}
