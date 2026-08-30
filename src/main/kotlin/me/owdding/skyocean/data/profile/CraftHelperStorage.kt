package me.owdding.skyocean.data.profile

import me.owdding.skyocean.features.recipe.Recipe
import me.owdding.skyocean.features.recipe.RepoApiRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.IngredientCraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.NormalCraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.RepoLibRecipeTree
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsMethod
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsRecipe
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.storage.ProfileStorage
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import kotlin.math.ceil

@LateInitModule
object CraftHelperStorage {
    private val storage = ProfileStorage<CraftHelperRecipe>(
        2,
        { NormalCraftHelperRecipe(null) },
        "craft_helper",
    ) { version ->
        when (version) {
            0 -> SkyOceanCodecs.NormalCraftHelperRecipeCodec.codec().xmap(
                { (item, amount) ->
                    NormalCraftHelperRecipe(
                        item?.id?.let { SkyBlockId.unknownType(it) },
                        amount,
                    )
                },
                { it },
            ).xmap({ it as CraftHelperRecipe }, { it as NormalCraftHelperRecipe })

            1 -> SkyOceanCodecs.NormalCraftHelperRecipeCodec.codec().xmap({ it as CraftHelperRecipe }, { it as NormalCraftHelperRecipe })

            2 -> SkyOceanCodecs.CraftHelperRecipeCodec.codec()
            else -> CodecHelpers.unit { NormalCraftHelperRecipe(null, 1) }
        }
    }

    val canModifyCount: Boolean get() = storage.get() is CraftHelperRecipe.MutableCount
    val recipeType get() = storage.get()?.type

    val data get() = storage.get()
    val selectedItem get() = data?.selectedItem
    val selectedAmount get() = data?.amount ?: 1

    fun set(recipe: CraftHelperRecipe) {
        storage.update(recipe)
    }

    fun setSelected(item: SkyBlockId?) {
        storage.update(NormalCraftHelperRecipe(item))
    }

    fun setAmount(amount: Int) {
        var amount = amount.coerceAtLeast(1)
        val data = data as? CraftHelperRecipe.MutableCount ?: return

        if (data is CraftHelperRecipe.MultiplesOf) {
            amount = ceil(amount.toFloat() / data.multiples).toInt() * data.multiples
        }

        storage.update(data.withAmount(amount))
    }

    fun setSkyShards(recipe: SkyShardsMethod) {
        storage.update(SkyShardsRecipe(recipe))
    }

    fun setRepoLibRecipe(recipe: RepoApiRecipe) {
        storage.update(RepoLibRecipeTree(recipe, recipe.output?.amount ?: 1))
    }

    fun clear() {
        storage.update(NormalCraftHelperRecipe(null))
    }

    fun save() {
        storage.save()
    }

    fun <T> addToIngredientRecipe(recipe: T) where T : CraftHelperRecipe, T : CraftHelperRecipe.Ingredients {
        getOrCreateIngredientRecipe().add(recipe.entriesForAddition.map { it.withAmount(it.amount * recipe.amount) })
    }

    fun getOrCreateIngredientRecipe(): IngredientCraftHelperRecipe {
        val data = data
        if (data is IngredientCraftHelperRecipe) {
            return data
        }

        return storage.update(IngredientCraftHelperRecipe()).apply {
            val inputs = (data as? CraftHelperRecipe.Ingredients)?.entriesForAddition ?: return@apply
            add(inputs.map { it.withAmount(it.amount * data.amount) })
        }
    }

}
