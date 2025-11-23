package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.items.accessory.AccessoryBagAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object AccessoryBagItemSource : ItemSource {
    override fun getAll() = AccessoryBagAPI.getItems().map { (stack, page) -> SimpleTrackedItem(stack, AccessoryBagItemContext(page)) }

    override val type = ItemSources.ACCESSORY_BAG
}

data class AccessoryBagItemContext(val page: Int) : ItemContext {
    override fun collectLines() = build {
        add("Accessory bag page $page") { color = TextColor.GRAY }
        requiresOverworld { add("Click to open accessories!") { color = TextColor.YELLOW } }
        riftWarning()
    }

    override val source = ItemSources.ACCESSORY_BAG

    override fun open() = requiresOverworld(true) { requiresCookie { McClient.sendCommand("/ab $page") } }
}
