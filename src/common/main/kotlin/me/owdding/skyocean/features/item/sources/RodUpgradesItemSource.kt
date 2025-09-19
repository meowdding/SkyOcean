package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.features.item.sources.system.ParentItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object RodUpgradesItemSource : ItemSource {
    override val type: ItemSources = ItemSources.ROD_UPGRADE
    override fun getAll(): List<SimpleTrackedItem> = emptyList()

    override fun postProcess(items: List<SimpleTrackedItem>): List<SimpleTrackedItem> {
        return items.filter { (itemStack) -> itemStack.getData(DataTypes.CATEGORY)?.name?.contains("rod", true) == true }
            .flatMap { item ->
                val (itemStack) = item
                listOfNotNull(
                    itemStack.getData(DataTypes.SINKER),
                    itemStack.getData(DataTypes.HOOK),
                    itemStack.getData(DataTypes.LINE),
                ).map { (_, id) -> SkyBlockId.item(id) }
                    .map { SimpleTrackedItem(it.toItem(), RodUpgradeItemContext(item)) }
            }
    }
}

data class RodUpgradeItemContext(override val parent: SimpleTrackedItem) : ParentItemContext(parent) {
    override val source: ItemSources = ItemSources.ROD_UPGRADE

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
}
