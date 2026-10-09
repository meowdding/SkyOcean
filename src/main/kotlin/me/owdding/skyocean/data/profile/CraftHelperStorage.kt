package me.owdding.skyocean.data.profile

import me.owdding.skyocean.SkyOcean
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
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import kotlin.math.ceil

@LateInitModule
object CraftHelperStorage {
    private val storage = SkyOcean.profileStorage<CraftHelperRecipe>(
        "craft_helper",
        defaultData = { NormalCraftHelperRecipe(null) },
        version = 2,
        codec = { version ->
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
    )

    val canModifyCount: Boolean get() = storage.get() is CraftHelperRecipe.MutableCount
    val recipeType get() = storage.get()?.type

    val data get() = storage.get()
    val selectedItem get() = data?.selectedItem
    val selectedAmount get() = data?.amount ?: 1

    fun set(recipe: CraftHelperRecipe) {
        storage.set(recipe)
        save()
    }

    fun setSelected(item: SkyBlockId?) {
        storage.set(NormalCraftHelperRecipe(item))
        save()
    }

    fun setAmount(amount: Int) {
        var amount = amount.coerceAtLeast(1)
        val data = data as? CraftHelperRecipe.MutableCount ?: return

        if (data is CraftHelperRecipe.MultiplesOf) {
            amount = ceil(amount.toFloat() / data.multiples).toInt() * data.multiples
        }

        storage.set(data.withAmount(amount))
        save()
    }

    fun setSkyShards(recipe: SkyShardsMethod) {
        storage.set(SkyShardsRecipe(recipe))
        save()
    }

    fun setRepoLibRecipe(recipe: RepoApiRecipe) {
        storage.set(RepoLibRecipeTree(recipe, recipe.output?.amount ?: 1))
        save()
    }

    fun clear() {
        storage.set(NormalCraftHelperRecipe(null))
        save()
    }

    fun save() {
        storage.save()
    }

    fun <T> addToIngredientRecipe(recipe: T) where T : CraftHelperRecipe, T : CraftHelperRecipe.Ingredients {
        getOrCreateIngredientRecipe().add(recipe.entriesForAddition.map { it.withAmount(it.amount * recipe.amount) })
        save()
    }

    fun getOrCreateIngredientRecipe(): IngredientCraftHelperRecipe {
        val data = data
        if (data is IngredientCraftHelperRecipe) {
            return data
        }

        val newRecipe = IngredientCraftHelperRecipe().apply {
            val inputs = (data as? CraftHelperRecipe.Ingredients)?.entriesForAddition ?: return@apply
            add(inputs.map { it.withAmount(it.amount * data.amount) })
        }
        storage.set(newRecipe)
        return newRecipe
    }

}
