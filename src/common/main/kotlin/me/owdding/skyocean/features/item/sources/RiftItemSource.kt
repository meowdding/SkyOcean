package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.data.profile.InventoryStorage
import me.owdding.skyocean.features.inventory.InventoryType
import me.owdding.skyocean.features.item.search.ItemContext
import me.owdding.skyocean.features.item.search.item.SimpleTrackedItem
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.profile.items.equipment.EquipmentAPI
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object RiftItemSource : ItemSource {
    override val type: ItemSources = ItemSources.RIFT

    override fun getAll(): List<SimpleTrackedItem> = buildList {
        addAll(StorageAPI.riftStorage.convert(::RiftEnderchestPage))
        addAll(EquipmentAPI.riftEquipment.map { (_, stack) -> SimpleTrackedItem(stack, RiftEquipment) })
        if (SkyBlockIsland.THE_RIFT.inIsland()) {
            addAll(McPlayer.inventory.map { SimpleTrackedItem(it, RiftInventoryContext) })
        } else {
            InventoryStorage.data?.get(InventoryType.RIFT)?.map { SimpleTrackedItem(it, RiftInventoryContext) }?.toMutableList()?.let { addAll(it) }
        }
    }
}

interface RiftItemContext : ItemContext {
    override val source: ItemSources get() = ItemSources.RIFT
    val clickText: MutableComponent? get() = null
    fun lines(): List<Component>

    override fun collectLines(): List<Component> = build {
        lines().addAll(this@RiftItemContext.lines())
        clickText?.let {
            if (SkyBlockIsland.THE_RIFT.inIsland()) {
                add(it)
            } else {
                add("Not currently in the rift!") { color = TextColor.RED }
            }
        }
    }

    fun requiresRift(runnable: () -> Unit) {
        if (!SkyBlockIsland.THE_RIFT.inIsland()) {
            Text.of("Requires to be in the rift!").sendWithPrefix()
            return
        }
        runnable()
    }
}

object RiftInventoryContext : RiftItemContext {
    override fun lines(): List<Component> = build {
        add("Rift Inventory")
    }
}

object RiftEquipment : RiftItemContext {
    override fun lines(): List<Component> = build {
        add("Equipped!")
    }

    override fun open() = requiresRift { McClient.sendCommand("/eq") }
}

data class RiftEnderchestPage(
    val index: Int,
) : RiftItemContext {
    override val clickText = Text.of("Click to open enderchest!") { color = TextColor.GRAY }

    override fun lines() = build {
        add("Enderchest Page $index") { color = TextColor.GRAY }
    }
    override fun open() = requiresRift { McClient.sendCommand("/bp $index") }
}
