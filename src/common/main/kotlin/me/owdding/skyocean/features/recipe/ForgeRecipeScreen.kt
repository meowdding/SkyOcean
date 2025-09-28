package me.owdding.skyocean.features.recipe

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.InventoryBuilder
import me.owdding.lib.extensions.toReadableTime
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.features.recipe.ForgeRecipeScreenHandler.forgeRecipes
import me.owdding.skyocean.helpers.ClientSideInventory
import me.owdding.skyocean.utils.Utils.itemBuilder
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.suggestions.SkyOceanSuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.world.item.Items
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.repolib.api.recipes.ingredient.ItemIngredient
import tech.thatgravyboat.repolib.api.recipes.ingredient.PetIngredient
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoRecipeAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds

class ForgeRecipeScreen(input: String) : ClientSideInventory("Forge", 6) {
    val skyblockId = SkyBlockId.fromName(input) ?: SkyBlockId.unknownType(input)
    val recipe = skyblockId?.cleanId?.uppercase()?.let(RepoRecipeAPI::getForgeRecipe)
    val forgeItemStack = skyblockId?.toItem() ?: itemBuilder(Items.BARRIER) {
        name("null")
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
                (RepoItemsAPI.getItemOrNull(input.id()) ?: Items.BARRIER.defaultInstance).apply {
                    this.count = input.count()
                }
            }
            val inputItemStacksWithSlots = slots.zip(inputItemStacks)

            inputItemStacksWithSlots.forEach { (index, item) ->
                add(index, item)
            }

            if (CraftHelperConfig.enabled && skyblockId != null) {
                add(32, Items.DIAMOND_PICKAXE) {
                    add(
                        Text.join(ChatUtils.ICON_SPACE_COMPONENT, "Craft Helper") {
                            this.color = TextColor.GREEN
                        },
                    )
                    add("Set as selected craft helper item!") {
                        this.color = TextColor.GRAY
                    }
                }
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

                    append("${recipe?.time()?.seconds?.toReadableTime()}") {
                        color = TextColor.YELLOW
                    }
                }
            }

            fill(Items.BLACK_STAINED_GLASS_PANE.defaultInstance.withTooltip())
        }.build()

        addItems(items)
        if (CraftHelperConfig.enabled) {
            slots[32].onClick = {
                CraftHelperStorage.setSelected(skyblockId)
                McScreen.self?.onClose()
            }
        }
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

object ForgeSuggestionProvider : SkyOceanSuggestionProvider {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource?>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions?>? {
        forgeRecipes.forEach { recipe ->
            when (recipe.result()) {
                is ItemIngredient -> {
                    (recipe.result() as ItemIngredient).let {
                        suggest(builder, it.id())
                        suggest(builder, RepoItemsAPI.getItemName(it.id()).stripped)
                    }
                }

                is PetIngredient -> {
                    (recipe.result() as PetIngredient).let {
                        suggest(builder, it.id())
                        suggest(builder, RepoItemsAPI.getItemName(it.id()).stripped)
                    }
                }

                else -> {}
            }
        }
        return builder.buildFuture()
    }
}
