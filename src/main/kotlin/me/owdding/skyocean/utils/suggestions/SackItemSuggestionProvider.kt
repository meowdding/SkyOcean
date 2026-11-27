package me.owdding.skyocean.utils.suggestions

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.repo.SackData
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import java.util.concurrent.CompletableFuture

object SackItemSuggestionProvider : SkyOceanSuggestionProvider {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource?>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions?>? {
        SackData.sackItems.forEach { (id, item) ->
            suggest(builder, id.cleanId.uppercase())
            suggest(builder, item.create().cleanName)
        }
        return builder.buildFuture()
    }
}
