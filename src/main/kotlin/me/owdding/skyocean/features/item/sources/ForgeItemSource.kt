package me.owdding.skyocean.features.item.sources

import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import me.owdding.skyocean.utils.Utils.mapNotNull
import tech.thatgravyboat.skyblockapi.api.profile.items.forge.ForgeAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.time.Instant

object ForgeItemSource : ItemSource, MeowddingLogger by SkyOcean.featureLogger() {
    override fun getAll() = ForgeAPI.getForgeSlots().mapNotNull(
        { (id) -> debug("Couldn't find item for $id") },
        { (slot, item) ->
            createFromIdAndAmount(SkyBlockId.unknownType(item.id), 1)?.let { SimpleTrackedItem(it, ForgeItemContext(slot, item.expiryTime)) }
        },
    )

    override val type = ItemSources.FORGE
}

data class ForgeItemContext(val slot: Int, val finishTime: Instant) : ItemContext {
    override val source = ItemSources.FORGE
    override fun collectLines() = build {
        add("Forge slot: $slot") { color = TextColor.GRAY }
    }
}
