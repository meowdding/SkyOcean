package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.data.profile.GalateaItemStorage
import me.owdding.skyocean.features.item.sources.system.ParentItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import me.owdding.skyocean.utils.tags.SkyblockItemTagKey
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
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


object ToolkitItemSource : ItemSource {
    override val type = ItemSources.TOOLKIT

    override fun getAll(): List<SimpleTrackedItem> = emptyList()

    override fun postProcess(items: List<SimpleTrackedItem>): List<SimpleTrackedItem> {
        return items.filter { (itemStack) -> itemStack.getSkyBlockId()?.contains("HUNTING_TOOLKIT", true) == true }.flatMap { item ->
            GalateaItemStorage.data?.toolkitItems?.map { SimpleTrackedItem(it, ToolkitItemContext(item)) } ?: emptyList()
        }
    }
}

data class ToolkitItemContext(override val parent: SimpleTrackedItem) : ParentItemContext(parent) {
    override val source: ItemSources = ItemSources.TOOLKIT

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


