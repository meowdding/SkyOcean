package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.features.item.sources.system.ParentItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import me.owdding.skyocean.utils.tags.SkyblockItemTagKey
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object DrillUpgradeItemSource : ItemSource {
    override fun getAll(): List<SimpleTrackedItem> = emptyList()
    override val type: ItemSources = ItemSources.DRILL_UPGRADE

    override fun postProcess(items: List<SimpleTrackedItem>): List<SimpleTrackedItem> {
        return items.filter { (itemStack) -> itemStack in SkyblockItemTagKey.DRILLS }
            .flatMap { item ->
                val (itemStack) = item
                listOfNotNull(
                    itemStack.getData(DataTypes.FUEL_TANK),
                    itemStack.getData(DataTypes.ENGINE),
                    itemStack.getData(DataTypes.UPGRADE_MODULE),
                ).map { SkyOceanItemId.item(it) }
                    .map { SimpleTrackedItem(it.toItem(), DrillItemContext(item)) }
            }
    }
}

data class DrillItemContext(override val parent: SimpleTrackedItem) : ParentItemContext(parent) {
    override fun collectLines(): List<Component> = build {
        add("Installed on ") {
            append(parent.itemStack.hoverName)
            append("!")
            color = TextColor.GRAY
        }
        lines().addAll(parent.context.collectLines())
    }

    override fun open() {
        parent.context.open()
    }

    override val source: ItemSources = parent.context.source
}
