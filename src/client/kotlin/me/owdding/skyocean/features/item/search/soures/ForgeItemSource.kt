package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import me.owdding.skyocean.utils.Utils.mapNotNull
import tech.thatgravyboat.skyblockapi.api.profile.mining.forge.ForgeAPI

object ForgeItemSource : ItemSource {
    override fun getAll() = ForgeAPI.getForgeSlots().mapNotNull(
        { (id) -> SkyOcean.warn("Couldn't find item for {}", id) },
        { (slot, item) ->
            createFromIdAndAmount(item.id, 1)?.let { SimpleTrackedItem(it, ForgeItemContext(slot)) }
        },
    )

    override fun remove(item: SimpleTrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.FORGE
}

data class ForgeItemContext(val slot: Int) : ItemContext {
    override val source = ItemSources.FORGE
    override fun collectLines() = build {
        add("Forge slot: $slot")
    }
}
