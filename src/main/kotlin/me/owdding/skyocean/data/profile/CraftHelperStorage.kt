package me.owdding.skyocean.data.profile

import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.CustomCraftHelperTree
import me.owdding.skyocean.features.recipe.crafthelper.data.NormalCraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsMethod
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsRecipe
import me.owdding.skyocean.features.recipe.custom.CustomRoot
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.storage.ProfileStorage
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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

    val canModifyCount: Boolean get() = storage.get()?.canModifyCount == true
    val recipeType get() = storage.get()?.type

    val data get() = storage.get()
    @OptIn(ExperimentalContracts::class)
    inline fun withData(runnable: (CraftHelperRecipe?) -> Unit) {
        contract {
            callsInPlace(runnable, InvocationKind.EXACTLY_ONCE)
        }
        runnable(data)
    }

    val selectedItem get() = (data as? CraftHelperRecipe.SkyblockId)?.resultId
    val selectedAmount
        get() = data?.amount ?: 1

    fun setSelected(item: SkyBlockId?) {
        storage.update(NormalCraftHelperRecipe(item))
    }

    fun setStorage(recipe: CraftHelperRecipe) {
        storage.update(recipe)
    }

    fun getAndOrSetCustomRecipe(): CustomCraftHelperTree {
        val data = data
        if (data == null || data !is CustomCraftHelperTree) {
            val newValue = CustomCraftHelperTree(CustomRoot(), mutableMapOf())
            storage.update(newValue)
            return newValue
        }
        return data
    }

    fun setAmount(amount: Int) = withData { data ->
        val amount = amount.coerceAtLeast(1)
        if (data !is CraftHelperRecipe.Amount) return
        storage.update(data.withAmount(amount))
    }

    fun setSkyShards(recipe: SkyShardsMethod) {
        storage.update(SkyShardsRecipe(recipe))
    }

    fun clear() {
        storage.update(NormalCraftHelperRecipe(null))
    }

    fun save() {
        storage.save()
    }
}
