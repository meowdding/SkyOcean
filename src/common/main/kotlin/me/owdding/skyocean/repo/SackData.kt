package me.owdding.skyocean.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.utils.Utils
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId

@Module
object SackData {
    val sackRegex = Regex("(LARGE|MEDIUM|SMALL)_")
    fun normalizedSackId(id: String) = id.replace(sackRegex, "")

    val data: List<Sack> = Utils.loadRepoData("sacks", CodecUtils::list)

    fun getByNormalizedId(id: String) = data.find { it.normalizedId == id }

    @GenerateCodec
    data class Sack(
        val sack: String,
        val items: List<String>,
    ) {
        val normalizedId by lazy { normalizedSackId(sack) }
        val item by RepoItemsAPI.getItemLazy(sack)
        val containingItems by lazy { items.map { RepoItemsAPI.getItem(it) } }

        operator fun contains(stack: ItemStack) = stack.getSkyBlockId() in items

    }
}
