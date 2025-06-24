package me.owdding.skyocean.commands

import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider

interface SkyOceanSuggestionProvider : SuggestionProvider<FabricClientCommandSource> {

    fun suggest(builder: SuggestionsBuilder, name: String) {
        val validChars = listOf(' ', '_', '-')
        val filtered = name.filter { it.isDigit() || it.isLetter() || it in validChars }.trim()
        if (SharedSuggestionProvider.matchesSubStr(builder.remaining.lowercase(), filtered.lowercase())) {
            builder.suggest(filtered)
        }
    }

}
