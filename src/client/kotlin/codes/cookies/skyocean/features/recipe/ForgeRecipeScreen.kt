package codes.cookies.skyocean.features.recipe

import codes.cookies.skyocean.features.recipe.ForgeRecipeScreenHandler.forgeRecipes
import codes.cookies.skyocean.helpers.ClientSideInventory
import me.owdding.ktmodules.Module
import codes.cookies.skyocean.utils.Utils.append
import codes.cookies.skyocean.utils.Utils.formatReadableTime
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.world.item.Items
import tech.thatgravyboat.lib.builder.InventoryBuilder
import tech.thatgravyboat.lib.extensions.withTooltip
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.PetIngredient
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.remote.PetQuery
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoRecipeAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds

class ForgeRecipeScreen(input: String) : ClientSideInventory("Forge", 6) {
    val id = RepoItemsAPI.getItemIdByName(input) ?: input
    val recipe = RepoRecipeAPI.getForgeRecipe(id)
    val forgeItemStack = RepoItemsAPI.getItemOrNull(id)
        ?: RepoPetsAPI.getPetAsItemOrNull(PetQuery(id, SkyBlockRarity.LEGENDARY, 100))
        ?: Items.BARRIER.defaultInstance.withTooltip {
            add("Item not found")
            add("ID: $id")
        }

    val sizes = mapOf(
        1 to listOf(10),
        2 to listOf(10, 11),
        3 to listOf(10, 11, 19),
        4 to listOf(10, 11, 19, 20),
        5 to listOf(10, 11, 19, 20, 28),
        6 to listOf(10, 11, 19, 20, 28, 29),
        7 to listOf(10, 11, 12, 19, 20, 28, 29),
        8 to listOf(10, 11, 12, 19, 20, 21, 28, 29),
        9 to listOf(10, 11, 12, 19, 20, 21, 28, 29, 30),
    )

    init {
        val items = InventoryBuilder().apply {
            val inputs = recipe?.inputs()?.filterIsInstance<ItemIngredient>() ?: emptyList()
            val slots = sizes[inputs.size] ?: emptyList()
            val inputItemStacks = inputs.map { input ->
                (RepoItemsAPI.getItemOrNull(input.id) ?: Items.BARRIER.defaultInstance).apply {
                    this.count = input.count
                }
            }
            val inputItemStacksWithSlots = slots.zip(inputItemStacks)

            inputItemStacksWithSlots.forEach { (index, item) ->
                add(index, item)
            }

            add(14, Items.FURNACE) {
                add("Forge Recipe") {
                    color = TextColor.GREEN
                }
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

            add(23, Items.CLOCK) {
                add("Time: ") {
                    color = TextColor.GREEN

                    append("${recipe?.time?.seconds?.formatReadableTime()}") {
                        color = TextColor.YELLOW
                    }
                }
            }

            fill(Items.BLACK_STAINED_GLASS_PANE.defaultInstance.withTooltip())
        }.build()

        addItems(items)
    }
}

@Module
object ForgeRecipeScreenHandler {
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
                        suggest(builder, RepoItemsAPI.getItemName(it.id).stripped)
                    }
                }

                else -> {}
            }
        }
        return builder.buildFuture()
    }

    private fun suggest(builder: SuggestionsBuilder, name: String) {
        val validChars = listOf(' ', '_', '-')
        val filtered = name.filter { it.isDigit() || it.isLetter() || it in validChars }.trim()
        if (SharedSuggestionProvider.matchesSubStr(builder.remaining.lowercase(), filtered.lowercase())) {
            builder.suggest(filtered)
        }
    }
}
