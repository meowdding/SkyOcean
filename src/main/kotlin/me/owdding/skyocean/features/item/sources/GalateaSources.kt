package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.data.profile.GalateaItemStorage
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.ParentItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.tags.SkyblockItemTagKey
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object HuntaxeItemSource : ItemSource {
    override val type = ItemSources.HUNT_AXE

    override fun getAll(): List<SimpleTrackedItem> = emptyList()

    override fun postProcess(items: List<SimpleTrackedItem>): List<SimpleTrackedItem> {
        return items.filter { (itemStack) -> itemStack.getSkyBlockId() in SkyblockItemTagKey.HUNT_AXES }.mapNotNull { item ->
            GalateaItemStorage.data?.huntaxeItem?.let {
                SimpleTrackedItem(it, HuntaxeItemContext(item))
            }
        }
    }
}

data class HuntaxeItemContext(override val parent: SimpleTrackedItem) : ParentItemContext(parent) {
    override val source: ItemSources = ItemSources.HUNT_AXE

    override fun collectLines(): List<Component> = build {
        add("Contained in ") {
            append(parent.itemStack.hoverName)
            append("!")
            color = TextColor.GRAY
        }
        lines().addAll(parent.context.collectLines())
    }

    override fun open() {
        parent.context.open()
    }
}


object HuntingToolkitItemSource : ItemSource, ItemContext {
    override val type = ItemSources.HUNTING_TOOLKIT
    override val source: ItemSources = ItemSources.HUNTING_TOOLKIT

    override fun getAll(): List<SimpleTrackedItem> = GalateaItemStorage.data?.toolkitItems?.map {
        SimpleTrackedItem(it, this)
    } ?: emptyList()

    override fun collectLines(): List<Component> = build {
        add("Contained in ") {
            append("Hunting Toolkit", CatppuccinColors.Mocha.green)
            append("!")
            color = TextColor.GRAY
        }
    }

    override fun open() = requiresOverworld(true) {
        McClient.sendCommand("huntingtoolkit")
    }
}



