package me.owdding.skyocean.features.item.sources

import me.owdding.skyocean.data.profile.InventoryStorage
import me.owdding.skyocean.features.inventory.InventoryType
import me.owdding.skyocean.features.item.sources.system.ItemContext
import me.owdding.skyocean.features.item.sources.system.SimpleTrackedItem
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.profile.items.equipment.EquipmentAPI
import tech.thatgravyboat.skyblockapi.api.profile.items.storage.StorageAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.getArmor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object RiftItemSource : ItemSource {
    override val type: ItemSources = ItemSources.RIFT

    override fun getAll(): List<SimpleTrackedItem> = buildList {
        addAll(StorageAPI.riftStorage.convert(::RiftEnderchestPageContext))
        addAll(EquipmentAPI.riftEquipment.map { (_, stack) -> SimpleTrackedItem(stack, RiftEquipment) })

        if (SkyBlockIsland.THE_RIFT.inIsland()) {
            addAll(McPlayer.inventory.map { SimpleTrackedItem(it, RiftInventoryContext) })
            McPlayer.self?.getArmor()?.forEach { add(SimpleTrackedItem(it, RiftEquipment)) }
        } else {
            val riftData = InventoryStorage.data?.get(InventoryType.RIFT)
            riftData?.inventory?.map { SimpleTrackedItem(it, RiftInventoryContext) }?.toMutableList()?.let { addAll(it) }
            riftData?.armour?.values?.map { SimpleTrackedItem(it, RiftEquipment) }?.let { addAll(it) }
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
}

interface AbstractRiftStorageContext : RiftItemContext

object RiftBundleContext : RiftItemContext {
    override val clickText: MutableComponent = Text.of("")
    override fun lines(): List<Component> = emptyList()
}

object RiftStorageContext : AbstractRiftStorageContext {
    override val clickText: MutableComponent = Text.of("Click to open!") { color = TextColor.GRAY }

    override fun lines(): List<Component> = build {
        add("Rift Enderchest") { color = TextColor.GRAY }
    }

    override fun open() = requiresRift(true) { McClient.sendCommand("/ec") }
}

object RiftInventoryContext : RiftItemContext {
    override fun lines(): List<Component> = build {
        add("Rift Inventory") { color = TextColor.GRAY }
    }
}

object RiftEquipment : RiftItemContext {
    override val clickText: MutableComponent = Text.of("Click to open equipment menu!") { color = TextColor.GRAY }

    override fun lines(): List<Component> = build {
        requiresRift { add("Equipped!") { color = TextColor.GRAY } }
        requiresOverworld { add("Equipped in rift!") { color = TextColor.GRAY } }
    }

    override fun open() = requiresRift(true) { McClient.sendCommand("/eq") }
}

data class RiftEnderchestPageContext(
    val index: Int,
) : AbstractRiftStorageContext {
    override val clickText = Text.of("Click to open enderchest!") { color = TextColor.GRAY }

    override fun lines() = build {
        requiresRift { add("Enderchest Page $index") { color = TextColor.GRAY } }
        requiresOverworld { add("Rift Enderchest Page $index") { color = TextColor.GRAY } }
    }

    override fun open() = requiresRift(true) { McClient.sendCommand("/ec $index") }
}
