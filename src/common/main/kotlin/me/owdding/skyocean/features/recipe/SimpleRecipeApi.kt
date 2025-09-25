package me.owdding.skyocean.features.recipe

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.helpers.McClient
import me.owdding.skyocean.features.recipe.RepoApiRecipe as RepoApiRecipeWrapper
import tech.thatgravyboat.repolib.api.recipes.Recipe as RepoApiRecipe

private val illegalIngredients = listOf(
    "DIAMOND_BLOCK",
    "IRON_BLOCK",
    "EMERALD_BLOCK",
    "COAL_BLOCK",
    "REDSTONE_BLOCK",
    "GOLD_BLOCK",
    "LAPIS_BLOCK",
    "HAY_BLOCK",
    "SLIME_BLOCK",
    "LEATHER",
    "BLAZE_POWDER",
    "STICK",
    "WOOD_SWORD",
    "WOOD_PICKAXE",
    "WOOD_SPADE",
    "WOOD_HOE",
    "WOOD_AXE",
)

@LateInitModule
object SimpleRecipeApi {

    internal val supportedTypes = arrayOf(
        RepoApiRecipe.Type.FORGE to RecipeType.FORGE,
        RepoApiRecipe.Type.CRAFTING to RecipeType.CRAFTING,
        RepoApiRecipe.Type.KAT to RecipeType.KAT,
    )

    internal val recipes = mutableListOf<Recipe>()
    internal val idToRecipes: MutableMap<SkyOceanItemId, List<Recipe>> = mutableMapOf()

    init {
        supportedTypes.forEach { (recipe, type) ->
            recipes += RepoAPI.recipes().getRecipes(recipe).map { recipe ->
                RepoApiRecipeWrapper(
                    recipe,
                    type,
                )
            }
        }
        recipes.removeIf {
            isBlacklisted(it).apply {
                if (this) {
                    SkyOcean.debug(
                        "Removing ${it.output?.skyblockId} with ${it.inputs.size} ingredients",
                    )
                }
            }
        }

        SkyOcean.trace("Loaded ${recipes.size} Recipes from repo api")
        val extraRecipes = Utils.loadRepoData("recipes", SkyOceanCodecs.CustomRecipeCodec.codec().listOf())
        recipes.addAll(extraRecipes)

        SkyOcean.trace("Loaded ${extraRecipes.size} extra from local repo")

        rebuildRecipes()

        McClient.runNextTick {
            val amount = recipes.flatMap {
                buildList {
                    add(it.output)
                    addAll(it.inputs)
                }.filterIsInstance<ItemLikeIngredient>()
            }.distinct().onEach { it.itemName }.count()
            SkyOcean.trace("Preloaded $amount items")
        }
    }

    fun rebuildRecipes() {
        idToRecipes.clear()
        idToRecipes.putAll(
            recipes.mapNotNull { recipe -> recipe.output?.id?.let { recipe to it } }
                .groupBy { it.second }
                .mapValues { (_, v) -> v.map { it.first } }
                .filter { (_, v) -> v.isNotEmpty() },
        )
        idToRecipes.putAll(idToRecipes.entries.associate { (key, value) -> SkyOceanItemId.unsafe(key.cleanId) to value })
    }

    fun hasRecipe(id: SkyOceanItemId) = idToRecipes.containsKey(id)

    fun getBestRecipe(ingredient: Ingredient) = (ingredient as? ItemLikeIngredient)?.id?.takeIf(::hasRecipe)?.let { getBestRecipe(it) }

    fun getBestRecipe(id: SkyOceanItemId): Recipe? {

        return runCatching {
            idToRecipes[id]?.firstOrNull()?.let { return@runCatching it }

            val variants = when {
                id.isPet -> SkyBlockRarity.entries.reversed().map { SkyOceanItemId.pet(id.cleanId, it.name) }
                id.isRune -> (3 downTo 0).map { SkyOceanItemId.rune(id.cleanId, it) }
                id.isEnchantment -> (10 downTo 0).map { SkyOceanItemId.enchantment(id.cleanId, it) }
                else -> emptyList()
            }
            variants.mapNotNull { idToRecipes[it] }.firstNotNullOfOrNull { it.firstOrNull() }?.let { return@runCatching it }
        }.getOrNull()
    }

    fun isBlacklisted(recipe: Recipe): Boolean {
        val inputs = recipe.inputs
        val itemInputs = inputs.filterIsInstance<ItemLikeIngredient>().map { it.skyblockId }.distinct()
        if (inputs.size == itemInputs.size && itemInputs.size == 1 && illegalIngredients.containsAll(itemInputs)) {
            return true
        }

        return illegalIngredients.contains(recipe.output?.skyblockId)
    }

    fun <K> MutableMap<K, Ingredient>.addOrPut(key: K, ingredient: Ingredient): Ingredient =
        merge(key, ingredient) { a, b -> a + b }!!
}

