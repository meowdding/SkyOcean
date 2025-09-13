package me.owdding.skyocean.data.profile

import com.mojang.serialization.Codec
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.NormalCraftHelperRecipe
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.storage.ProfileStorage

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
                        item?.id?.let { SkyOceanItemId.unknownType(it) },
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

    val data get() = storage.get()
    val selectedItem
        get() = when (data) {
            is NormalCraftHelperRecipe -> (data as NormalCraftHelperRecipe).item
            else -> null
        }
    val selectedAmount
        get() = when (data) {
            is NormalCraftHelperRecipe -> (data as NormalCraftHelperRecipe).amount
            else -> 1
        }

    fun setSelected(item: SkyOceanItemId?) {
        storage.set(NormalCraftHelperRecipe(item))
        save()
    }

    fun setAmount(amount: Int) {
        val amount = amount.coerceAtLeast(1)
        val changed = when (val current = data) {
            is NormalCraftHelperRecipe -> {
                storage.set(NormalCraftHelperRecipe(current.item, amount))
                true
            }

            else -> false
        }

        if (changed) save()
    }

    fun clear() {
        storage.set(NormalCraftHelperRecipe(null))
        save()
    }

    fun save() {
        storage.save()
    }
}
