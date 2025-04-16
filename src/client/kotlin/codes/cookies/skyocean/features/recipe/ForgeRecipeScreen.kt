package codes.cookies.skyocean.features.recipe

import codes.cookies.skyocean.helpers.ClientSideInventory
import codes.cookies.skyocean.helpers.InventoryBuilder
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.Utils.append
import codes.cookies.skyocean.utils.withTooltip
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.world.item.Items
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.PetIngredient
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture

class ForgeRecipeScreen(val id: String) : ClientSideInventory("Forge", 6) {
    val forgeItemStack = RepoItemsAPI.getItemOrNull(id) ?: RepoItemsAPI.getItem(RepoItemsAPI.getItemIdByName(id) ?: id)

    init {
        val items = InventoryBuilder().apply {
            add(14, Items.FURNACE) {
                add("Forge")
                add("<-- ") {
                    color = TextColor.WHITE
                    append("Required Items") {
                        color = TextColor.YELLOW
                    }
                }
                add("      Result item ") {
                    color = TextColor.YELLOW
                    append("-->") {
                        color = TextColor.WHITE
                    }
                }
            }
            add(16, forgeItemStack)

            fill(Items.BLACK_STAINED_GLASS_PANE.defaultInstance.withTooltip())
        }.build()

        addItems(items)
    }

    @Module
    companion object {
        val forgeRecipes by lazy { RepoAPI.recipes().getRecipes(Recipe.Type.FORGE) }

        @Subscription
        fun onCommand(event: RegisterCommandsEvent) {
            event.register("viewforgerecipe") {
                then("recipe", StringArgumentType.greedyString(), ForgeSuggestionProvider) {
                    callback {
                        McClient.setScreen(ForgeRecipeScreen(this.getArgument("recipe", String::class.java)))
                    }
                }
            }
        }

        object ForgeSuggestionProvider : SuggestionProvider<FabricClientCommandSource> {
            override fun getSuggestions(
                context: CommandContext<FabricClientCommandSource?>,
                builder: SuggestionsBuilder,
            ): CompletableFuture<Suggestions?>? {
                forgeRecipes.forEach { recipe ->
                    when (recipe.result) {
                        is ItemIngredient -> {
                            (recipe.result() as ItemIngredient).let {
                                suggest(builder, it.id)
                                suggest(builder, RepoItemsAPI.getItemName(it.id).stripped)
                            }
                        }

                        is PetIngredient -> {
                            (recipe.result() as PetIngredient).let {
                                suggest(builder, it.id)
                                // TODO: Add pet name suggestion
                            }
                        }

                        else -> {}
                    }
                }
                return builder.buildFuture()
            }

            private fun suggest(builder: SuggestionsBuilder, name: String) {
                val filtered = name.filter { it.isDigit() || it.isLetter() || it == ' ' || it == '_' }.trim()
                if (SharedSuggestionProvider.matchesSubStr(builder.remaining.lowercase(), filtered.lowercase())) {
                    builder.suggest(filtered)
                }
            }
        }
    }
}
