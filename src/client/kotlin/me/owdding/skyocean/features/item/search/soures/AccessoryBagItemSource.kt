package me.owdding.skyocean.features.item.search.soures

import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.accessory.AccessoryBagAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient

object AccessoryBagItemSource : ItemSource {
    override fun getAll() = AccessoryBagAPI.getItems().map { (stack, page) -> SimpleTrackedItem(stack, AccessoryBagItemContext(page)) }

    override fun remove(item: SimpleTrackedItem) {
        TODO("Not yet implemented")
    }

    override val type = ItemSources.ACCESSORY_BAG
}

data class AccessoryBagItemContext(val page: Int) : ItemContext {
    override fun collectLines() = build {
        add("Accessory bag page $page")
    }

    override val source = ItemSources.ACCESSORY_BAG

    override fun open() = requiresCookie { McClient.sendCommand("/ab $page") }
}
