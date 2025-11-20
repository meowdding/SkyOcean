package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import me.owdding.skyocean.utils.Utils.not
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.profile.hunting.AttributeAPI

object HuntingBoxItemSource : ItemSource {
    override fun getAll(): List<SimpleTrackedItem> = buildList {
        AttributeAPI.attributeMap.forEach { (id, data) ->
            add(SimpleTrackedItem(id.toItem().copyWithCount(data.owned), HuntingBoxItemContext))
        }
    }

    override val type: ItemSources = ItemSources.HUNTING_BOX
}

object HuntingBoxItemContext : ItemContext {
    override fun collectLines(): List<Component> = listOf(!"In your hunting box!")

    override val source: ItemSources = ItemSources.HUNTING_BOX
}
