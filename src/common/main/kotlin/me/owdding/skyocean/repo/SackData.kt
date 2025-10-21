package me.owdding.skyocean.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.PreInitModule
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.extensions.setUnlessNull
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import java.util.concurrent.atomic.AtomicReference

@PreInitModule
object SackData {
    val sackRegex = Regex("(LARGE|MEDIUM|SMALL)_")
    private val _data = AtomicReference<List<Sack>>(emptyList())
    val data: List<Sack> get() = _data.get()

    fun normalizedSackId(id: String) = id.replace(sackRegex, "")

    fun getByNormalizedId(id: String) = data.find { it.normalizedId == id }

    @Subscription(FinishRepoLoadingEvent::class)
    fun onRepoLoad() {
        _data.setUnlessNull(Utils.loadRemoteRepoData("sacks", SkyOceanCodecs.SackCodec.codec().listOf()))
    }

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
