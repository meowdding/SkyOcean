package me.owdding.skyocean.features.recipe.crafthelper

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.commands.SkyOceanSuggestionProvider
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.recipe.crafthelper.visitors.RecipeVisitor
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils.getArgument
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture

val illegalIngredients = listOf(
    "DIAMOND_BLOCK",
    "IRON_BLOCK",
    "EMERALD_BLOCK",
    "COAL_BLOCK",
    "REDSTONE_BLOCK",
    "GOLD_BLOCK",
    "LAPIS_BLOCK",
    "HAY_BLOCK",
    "SLIME_BLOCK",
)

@LateInitModule
object SimpleRecipeApi {

    internal val supportedTypes = arrayOf(Recipe.Type.FORGE, Recipe.Type.CRAFTING)
    internal val recipes = mutableListOf<Recipe<*>>()
    internal val idToRecipes: Map<String, List<Recipe<*>>>

    init {
        supportedTypes.forEach { recipeType ->
            recipes += RepoAPI.recipes().getRecipes(recipeType)
        }
        recipes.removeIf {
            isBlacklisted(it).apply {
                if (this) {
                    SkyOcean.debug(
                        "Removing ${RecipeVisitor.getOutput(it)?.skyblockId} with ${
                            RecipeVisitor.getInputs(
                                it
                            ).size
                        } ingredients"
                    )
                }
            }
        }
        SkyOcean.trace("Loaded ${recipes.size} Recipes from repo api")

        idToRecipes = recipes.mapNotNull { recipe -> RecipeVisitor.getOutput(recipe)?.skyblockId?.let { recipe to it } }
            .groupBy { it.second }
            .mapValues { (k, v) -> v.map { it.first } }
            .filter { (_, v) -> v.isNotEmpty() }
    }

    fun hasRecipe(id: String) = idToRecipes.containsKey(id)

    fun getBestRecipe(ingredient: Ingredient) =
        (ingredient as? ItemLikeIngredient)?.skyblockId?.takeIf(::hasRecipe)?.let { getBestRecipe(it) }

    fun getBestRecipe(id: String): Recipe<*>? {
        assert(hasRecipe(id)) { "Item has no recipe" }
        runCatching {
            return idToRecipes[id]!!.firstOrNull()!!
        }.getOrElse {
            return null
        }
    }

    fun isBlacklisted(recipe: Recipe<*>): Boolean {
        val inputs = RecipeVisitor.getInputs(recipe)
        val itemInputs = inputs.filterIsInstance<ItemLikeIngredient>().map { it.skyblockId }.distinct()
        if (inputs.size == itemInputs.size && itemInputs.size == 1 && illegalIngredients.containsAll(itemInputs)) {
            return true
        }

        return illegalIngredients.contains(RecipeVisitor.getOutput(recipe)?.skyblockId)
    }

    @Subscription
    fun debug(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("test recipeDebug") {
            then("pretty") {
                then("recipe", StringArgumentType.greedyString(), IdSuggestions) {
                    callback {
                        val arg = this.getArgument<String>("recipe") ?: run {
                            Text.of("null :(") { this.color = TextColor.RED }
                            return@callback
                        }
                        val recipe = getBestRecipe(arg) ?: run {
                            Text.of("No recipe found for $arg!") { this.color = TextColor.RED }
                            return@callback
                        }
                        val output = RecipeVisitor.getOutput(recipe) ?: run {
                            Text.of("Recipe output is null!") { this.color = TextColor.RED }
                            return@callback
                        }
                        ContextAwareRecipeTree(recipe, output, 3).visit { node, depth ->
                            Text.of {
                                append(" ".repeat(depth))
                                when (node) {
                                    is ContextAwareRecipeTree -> append(
                                        node.output.withAmount(node.amount).serializeWithAmount()
                                    )

                                    is RecipeNode -> {
                                        append(node.output.withAmount(node.requiredAmount).serializeWithAmount())
                                        append(" (${node.requiredCrafts})")
                                    }

                                    is LeafNode -> append("L: ${node.output.serializeWithAmount()}")
                                }
                            }.send()
                        }
                    }
                }
            }

            then("recipe", StringArgumentType.greedyString(), IdSuggestions) {
                callback {
                    val arg = this.getArgument<String>("recipe") ?: run {
                        Text.of("null :(") { this.color = TextColor.RED }
                        return@callback
                    }
                    val recipe = getBestRecipe(arg) ?: run {
                        Text.of("No recipe found for $arg!") { this.color = TextColor.RED }
                        return@callback
                    }
                    val output = RecipeVisitor.getOutput(recipe) ?: run {
                        Text.of("Recipe output is null!") { this.color = TextColor.RED }
                        return@callback
                    }
                    val rawMaterial = mutableMapOf<String, Ingredient>()
                    RecipeTree(output).visit(false) { recipeNode, depth, children ->
                        if (children != 0) {
                            return@visit
                        }
                        rawMaterial.addOrPut(recipeNode.ingredient.serialize(), recipeNode.ingredient)
                    }
                    rawMaterial.forEach { (_, value) ->
                        Text.of("${value.serialize()}: ${value.amount.toFormattedString()}").sendWithPrefix()
                    }
                }
            }
        }
    }

    fun <K> MutableMap<K, Ingredient>.addOrPut(key: K, ingredient: Ingredient): Ingredient =
        merge(key, ingredient) { a, b -> a + b }!!
}

private object IdSuggestions : SkyOceanSuggestionProvider {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource?>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions?>? {
        SimpleRecipeApi.recipes.forEach { recipe ->
            RecipeVisitor.getOutput(recipe)?.let {
                suggest(builder, it.skyblockId)
            }
        }
        return builder.buildFuture()
    }
}
