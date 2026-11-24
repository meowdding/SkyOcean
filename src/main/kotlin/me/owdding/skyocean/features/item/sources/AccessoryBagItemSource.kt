package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import tech.thatgravyboat.skyblockapi.api.profile.maxwell.MaxwellAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

object AccessoryBagItemSource : ItemSource {
    override fun getAll() = MaxwellAPI.accessories.map { stack -> SimpleTrackedItem(stack, AccessoryBagItemContext) }

    override val type = ItemSources.ACCESSORY_BAG
}

data object AccessoryBagItemContext : ItemContext {
    override fun collectLines() = build {
        add("Accessory bag", TextColor.GRAY)
        requiresOverworld { add("Click to open accessories!", TextColor.YELLOW) }
        riftWarning()
    }

    override val source = ItemSources.ACCESSORY_BAG

    override fun open() = requiresOverworld(true) { requiresCookie { McClient.sendCommand("/ab $page") } }
}
