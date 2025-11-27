package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.data.profile.SackOfSacksItemStorage
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object SackOfSacksItemSource : ItemSource {
    override fun getAll(): List<SimpleTrackedItem> = SackOfSacksItemStorage.items.map { SimpleTrackedItem(it, SackOfSacksItemContext) }

    override val type: ItemSources = ItemSources.SACK_OF_SACKS
}

object SackOfSacksItemContext : ItemContext {
    override fun collectLines(): List<Component> = build {
        add("In your Sack of Sacks!") {
            this.color = TextColor.GRAY
        }
        add("Click to open!") {
            this.color = TextColor.YELLOW
        }
    }

    override fun open() = requiresOverworld(true) { McClient.sendCommand("/sax") }

    override val source: ItemSources = ItemSources.SACK_OF_SACKS

}
