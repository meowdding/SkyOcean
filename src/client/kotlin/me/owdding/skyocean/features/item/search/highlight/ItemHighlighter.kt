package me.owdding.skyocean.features.item.search.highlight

import com.teamresourceful.resourcefullib.common.color.Color
import kotlinx.coroutines.*
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.ItemSearchComponent
import me.owdding.skyocean.events.ItemStackCreateEvent
import me.owdding.skyocean.features.item.search.search.ItemFilter
import me.owdding.skyocean.utils.rendering.RenderUtils.renderBox
import net.minecraft.core.BlockPos
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.helpers.McClient
import kotlin.time.Duration.Companion.seconds

@Module
object ItemHighlighter {

    private var currentSearch: ItemFilter? = null
    private val allItems = mutableSetOf<ItemStack>()
    private var future: Job? = null
    private var chests: Iterable<BlockPos>? = null

    fun setHighlight(filter: ItemFilter?, chests: Iterable<BlockPos> = emptyList()) = McClient.tell {
        allItems.forEach { it.replaceVisually(null) }
        currentSearch = filter
        this.chests = chests
        cancelOrScheduleClear()
    }

    fun cancelOrScheduleClear() {
        future?.cancel(CancellationException("Item search has been canceled"))
        future = CoroutineScope(Dispatchers.Default).launch {
            delay(10.seconds)
            resetSearch()
        }
        future?.start()
    }

    fun resetSearch() = setHighlight(null)

    @Subscription
    @OptIn(ItemSearchComponent::class)
    fun onItem(event: ItemStackCreateEvent) {
        val filter = currentSearch ?: return
        if (filter.test(event.itemStack)) {
            event.itemStack.replaceVisually {
                copyFrom(event.itemStack)
                backgroundItem = Items.RED_STAINED_GLASS_PANE.defaultInstance
            }
            allItems.add(event.itemStack)
        }
    }

    @Subscription
    fun RenderWorldEvent.AfterTranslucent.renderWorld() {
        atCamera {
            chests?.forEach { block ->
                renderBox(block, ARGB.color(125, Color.RAINBOW.value).toUInt())
            }
        }
    }

}
