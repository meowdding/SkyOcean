package me.owdding.skyocean.features.item.search.highlight

import me.owdding.ktmodules.Module
import me.owdding.lib.compat.REIRuntimeCompatability
import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.item.search.search.SearchItemFilter
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI

@Module
object ReiItemHighlighter {

    private var highlighting: Boolean = false
    private var lastSearch: String? = null

    private val config get() = MiscConfig.useReiSearchBar

    @JvmStatic
    fun shouldStopREIHighlight(): Boolean = LocationAPI.isOnSkyBlock && config

    @OnlyOnSkyBlock
    @Subscription(TickEvent::class)
    fun onTick() {
        val search = REIRuntimeCompatability.getCurrentSearchBar() ?: return
        if (!REIRuntimeCompatability.isSearchBarHighlighting()) {
            stop()
            return
        }
        if (search == lastSearch) return
        lastSearch = search
        if (search.isBlank() || !config) {
            stop()
            return
        }
        highlighting = true
        // TODO: maybe add some search tags, so you can do more advanced searching
        //  (for example, searching by id, item category/rarity, etc)
        ItemHighlighter.setHighlight(SearchItemFilter(search), scheduleClear = false)
    }

    fun stop() {
        if (!highlighting) return
        highlighting = false
        lastSearch = ""
        ItemHighlighter.resetSearch()
    }

}
