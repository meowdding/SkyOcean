package me.owdding.skyocean.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.utils.PreInitModule
import me.owdding.skyocean.utils.RemoteRepoDelegate
import me.owdding.skyocean.utils.codecs.CodecHelpers
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId

@PreInitModule
object SackData {
    val sackRegex = Regex("(LARGE|MEDIUM|SMALL)_")

    val data: List<Sack>? by RemoteRepoDelegate.load("sacks", CodecHelpers.list<Sack>())

    fun normalizedSackId(id: String) = id.replace(sackRegex, "")

    fun getByNormalizedId(id: String) = data?.find { it.normalizedId == id }

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
