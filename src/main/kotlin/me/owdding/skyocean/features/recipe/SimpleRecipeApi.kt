package me.owdding.skyocean.features.recipe

import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.extensions.addAll
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RepoStatusEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockItemId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import me.owdding.skyocean.features.recipe.RepoApiRecipe as RepoApiRecipeWrapper
import tech.thatgravyboat.repolib.api.recipes.Recipe as RepoApiRecipe

@LateInitModule
object SimpleRecipeApi {

    internal val supportedTypes = arrayOf(
        RepoApiRecipe.Type.FORGE to RecipeType.FORGE,
        RepoApiRecipe.Type.CRAFTING to RecipeType.CRAFTING,
        RepoApiRecipe.Type.KAT to RecipeType.KAT,
        RepoApiRecipe.Type.SHOP to RecipeType.SHOP,
    )

    internal val illegalIngredients = CopyOnWriteArrayList<SkyBlockItemId>()
    internal val recipes = CopyOnWriteArrayList<Recipe>()
    internal val idToRecipes: MutableMap<SkyBlockId, List<Recipe>> = ConcurrentHashMap()
    internal val illegalShopRecipes = CopyOnWriteArrayList<SkyBlockItemId>()

    @Subscription(FinishRepoLoadingEvent::class, RepoStatusEvent::class)
    fun onRepoLoad() {
        recipes.clear()
        idToRecipes.clear()
        illegalIngredients.clear()
        illegalShopRecipes.clear()

        illegalIngredients.addAll(Utils.loadRemoteRepoData("skyocean/illegal_ingredients", CodecUtils::list))
        SkyOcean.debug("Loaded ${illegalIngredients.size} illegal ingredients")
        illegalShopRecipes.addAll(Utils.loadRemoteRepoData("skyocean/illegal_shop_recipes", CodecUtils::list))
        SkyOcean.debug("Loaded ${illegalShopRecipes.size} illegal shop recipes")

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
                    SkyOcean.trace(
                        "Removing ${it.output?.skyblockId} with ${it.inputs.size} ingredients",
                    )
                }
            }
        }

        SkyOcean.debug("Loaded ${recipes.size} Recipes from repo api")
        val extra = Utils.loadRemoteRepoData("skyocean/recipes", SkyOceanCodecs.CustomRecipeCodec.codec().listOf())
        recipes.addAll(extra)
        SkyOcean.debug("Loaded ${extra?.size ?: 0} extra from remote repo, new total is ${recipes.size}")

        rebuildRecipes()

        McClient.runNextTick {
            val amount = recipes.flatMap {
                buildList {
                    add(it.output)
                    addAll(it.inputs)
                }.filterIsInstance<ItemLikeIngredient>()
            }.distinct().onEach { it.itemName }.count() // calls itemName to construct the itemstacks
            SkyOcean.debug("Preloaded $amount items")
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
        idToRecipes.putAll(idToRecipes.entries.associate { (key, value) -> SkyBlockId.unsafe(key.cleanId) to value })
    }

    fun hasRecipe(id: SkyBlockId) = idToRecipes.containsKey(id)

    fun getBestRecipe(ingredient: Ingredient) = (ingredient as? ItemLikeIngredient)?.id?.takeIf(::hasRecipe)?.let { getBestRecipe(it) }

    fun getBestRecipe(id: SkyBlockId): Recipe? {

        return runCatching {
            idToRecipes[id]?.firstOrNull()?.let { return@runCatching it }

            val variants = when {
                id.isPet -> SkyBlockRarity.entries.reversed().map { SkyBlockId.pet(id.cleanId, it.name) }
                id.isRune -> (3 downTo 0).map { SkyBlockId.rune(id.cleanId, it) }
                id.isEnchantment -> (10 downTo 0).map { SkyBlockId.enchantment(id.cleanId, it) }
                else -> emptyList()
            }
            variants.mapNotNull { idToRecipes[it] }.firstNotNullOfOrNull { it.firstOrNull() }?.let { return@runCatching it }
        }.getOrNull()
    }

    fun isBlacklisted(recipe: Recipe): Boolean {
        if (recipe.recipeType == SHOP) {
            return illegalShopRecipes.contains(recipe.output?.id)
        }
        val inputs = recipe.inputs
        val itemInputs = inputs.filterIsInstance<ItemLikeIngredient>().map { it.id }.distinct()
        if (inputs.size == itemInputs.size && itemInputs.size == 1 && illegalIngredients.containsAll(itemInputs)) {
            return true
        }

        return illegalIngredients.contains(recipe.output?.id)
    }

    fun <K> MutableMap<K, Ingredient>.addOrPut(key: K, ingredient: Ingredient): Ingredient =
        merge(key, ingredient) { a, b -> a + b }!!
}

