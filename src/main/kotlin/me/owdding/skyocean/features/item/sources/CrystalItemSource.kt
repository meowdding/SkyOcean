package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.hotm.CrystalAPI
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object CrystalItemSource : ItemSource {
    override fun getAll() = CrystalAPI.crystals.mapNotNull { (type, _) ->
        if (CrystalAPI.isFound(type)) {
            createFromIdAndAmount(type.id, 1)?.let { SimpleTrackedItem(it, CrystalItemContext) }
        } else null
    }

    override val type = ItemSources.CRYSTAL
}

data object CrystalItemContext : ItemContext {
    override val source = ItemSources.CRYSTAL
    override fun collectLines() = build {
        add("Heart of the Mountain Crystal") { color = TextColor.GRAY }
    }
}
