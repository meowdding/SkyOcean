package me.owdding.skyocean.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.utils.PreInitModule
import me.owdding.skyocean.utils.RemoteRepoDelegate
import me.owdding.skyocean.utils.codecs.CodecHelpers
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.repo.LazyItemStack
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId

@PreInitModule
object SackData {
    val sackRegex = Regex("(LARGE|MEDIUM|SMALL)_")

    val data: List<Sack>? by RemoteRepoDelegate.load("sacks", CodecHelpers.list<Sack>())

    private var cachedSource: List<Sack>? = null
    private var cachedItems: Map<SkyBlockId, LazyItemStack> = emptyMap()

    val sackItems: Map<SkyBlockId, LazyItemStack>
        get() {
            val source = data ?: return emptyMap()
            if (source !== cachedSource) {
                cachedItems = source.flatMap { it.containingItems }
                    .mapNotNull { lazyStack ->
                        val stack = lazyStack.create()
                        SkyBlockId.fromItem(stack)?.let { id -> Triple(id, lazyStack, stack.cleanName) }
                    }
                    .sortedBy { it.third }
                    .associate { it.first to it.second }
                cachedSource = source
            }
            return cachedItems
        }

    fun amountOf(id: SkyBlockId): Int = SacksAPI.sackItems.entries.firstOrNull { it.key.equals(id.cleanId, ignoreCase = true) }?.value ?: 0
    fun normalizedSackId(id: String) = id.replace(sackRegex, "")

    fun getByNormalizedId(id: String) = data?.find { it.normalizedId == id }

    @GenerateCodec
    data class Sack(
        val sack: String,
        val items: List<String>,
    ) {
        val normalizedId by lazy { normalizedSackId(sack) }
        val item = SkyBlockItemsRepo.getLazyItemStack(sack)
        val containingItems = items.mapNotNull { SkyBlockItemsRepo.getLazyItemStack(it) }

        operator fun contains(stack: ItemStack) = stack.getSkyBlockId() in items

    }
}
