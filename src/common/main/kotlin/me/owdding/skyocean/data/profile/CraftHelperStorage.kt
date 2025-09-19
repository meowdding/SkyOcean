package me.owdding.skyocean.data.profile

import com.mojang.serialization.Codec
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.NormalCraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.SkyShardsMethod
import me.owdding.skyocean.features.recipe.crafthelper.SkyShardsRecipe
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.storage.ProfileStorage
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

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
            else -> Codec.unit { NormalCraftHelperRecipe(null, 1) }
        }
    }

    val canModifyCount: Boolean get() = storage.get()?.canModifyCount == true
    val recipeType get() = storage.get()?.type

    val data get() = storage.get()
    val selectedItem
        get() = when (val data = data) {
            is NormalCraftHelperRecipe -> data.item
            else -> null
        }
    val selectedAmount
        get() = when (val data = data) {
            is NormalCraftHelperRecipe -> data.amount
            is SkyShardsRecipe -> data.tree.quantity
            else -> 1
        }

    fun setSelected(item: SkyBlockId?) {
        storage.set(NormalCraftHelperRecipe(item))
        save()
    }

    fun setAmount(amount: Int) {
        val amount = amount.coerceAtLeast(1)
        when (val data = data) {
            is NormalCraftHelperRecipe -> storage.set(data.copy(amount = amount))
            else -> return
        }

        save()
    }

    fun setSkyShards(recipe: SkyShardsMethod) {
        storage.set(SkyShardsRecipe(recipe))
        save()
    }

    fun clear() {
        storage.set(NormalCraftHelperRecipe(null))
        save()
    }

    fun save() {
        storage.save()
    }
}
