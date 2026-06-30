package me.owdding.skyocean.data.profile

import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import me.owdding.skyocean.features.recipe.SimpleRecipeApi.getBestRecipe
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.NormalCraftHelperRecipe
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsMethod
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsRecipe
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.storage.ProfileStorage
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

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
    val selectedItem
        get() = when (val data = data) {
            is NormalCraftHelperRecipe -> data.item
            is SkyShardsRecipe -> data.tree.shard
            else -> null
        }
    val selectedAmount
        get() = when (val data = data) {
            is NormalCraftHelperRecipe -> data.amount
            is SkyShardsRecipe -> data.tree.quantity
            else -> 1
        }

    fun setSelectedWithNextUpAmount(item: SkyBlockId?, amount: Int) {
        setSelected(item)
        val craftAmount = item?.let(SimpleRecipeApi::getBestRecipe)?.output?.amount ?: 1
        setAmount(Utils.nextUp(amount, craftAmount))
        save()
        text("Set current recipe to ") {
            append("${selectedAmount}x ") { color = TextColor.GREEN }
            append(selectedItem?.toItem()?.let(ItemStack::getHoverName) ?: !"unknown")
            append("!")
        }.sendWithPrefix()
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
