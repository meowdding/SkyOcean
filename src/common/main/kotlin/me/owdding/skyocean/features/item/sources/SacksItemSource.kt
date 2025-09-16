package me.owdding.skyocean.features.item.sources

import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import me.owdding.skyocean.utils.Utils.mapNotNull
import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object SacksItemSource : ItemSource, MeowddingLogger by SkyOcean.featureLogger() {
    override fun getAll() = SacksAPI.sackItems.mapNotNull(
        { (_) -> },
        { (id, amount) -> createFromIdAndAmount(SkyOceanItemId.unknownType(id), amount) },
    ).map { SimpleTrackedItem(it, SackItemContext) }

    override val type = ItemSources.SACKS
}

object SackItemContext : ItemContext {

    override val source = ItemSources.SACKS

    override fun collectLines() = build {
        add("Sacks") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open sacks!") { this.color = TextColor.YELLOW } }
        riftWarning()
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("sacks") }
}
