package me.owdding.skyocean.features.misc

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.data.profile.IslandChestStorage
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.Utils.plus
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.tags.BlockTagKey
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.Slot
import net.minecraft.world.level.block.DoubleBlockCombiner
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.ChestType
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.level.BlockChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.RightClickBlockEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped

@Module
object ChestTracker {
    private var first: BlockPos? = null
    private var second: BlockPos? = null
    private var container: List<Slot>? = null

    private fun resetCoords() {
        first = null
        second = null
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onBlockInteract(event: RightClickBlockEvent) {
        val blockState = McLevel[event.pos]
        if (blockState !in BlockTagKey.CHESTS) return
        val chestType = blockState.getValue(BlockStateProperties.CHEST_TYPE)
        val type = getType(chestType)
        first = event.pos
        if (chestType !== ChestType.SINGLE) {
            var diff = when (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction.EAST -> BlockPos(0, 0, 1)
                Direction.WEST -> BlockPos(0, 0, -1)
                Direction.NORTH -> BlockPos(1, 0, 0)
                Direction.SOUTH -> BlockPos(-1, 0, 0)
                else -> BlockPos.ZERO
            }

            if (chestType !== ChestType.LEFT) {
                diff = diff.multiply(-1)
            }
            val doubleChestPosition: BlockPos = this.first!!.plus(diff)
            if (type == DoubleBlockCombiner.BlockType.FIRST) {
                this.second = doubleChestPosition
            } else {
                this.second = this.first
                this.first = doubleChestPosition
            }
        }

    }

    @Subscription
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    private fun BlockChangeEvent.onBlockBreak() {
        if (McLevel[pos] in BlockTagKey.CHESTS && state !in BlockTagKey.CHESTS) {
            IslandChestStorage.removeBlock(pos)
        }
    }

    @Subscription
    @MustBeContainer
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    private fun InventoryChangeEvent.onInventoryChange() {
        val mutable = this.titleComponent as? MutableComponent
        val contents = mutable?.contents as? TranslatableContents
        if (contents?.key?.startsWith("container.chest") != true) {
            if (title == "Minion Chest") {
                val first = first ?: return
                if (IslandChestStorage.hasBlock(first)) IslandChestStorage.removeBlock(first)
            }

            return
        }
        container = this.inventory
    }

    @Subscription(event = [ContainerCloseEvent::class])
    @OnlyIn(SkyBlockIsland.PRIVATE_ISLAND)
    fun onClose() {
        val container = container ?: return
        val first = first ?: return
        val second = second
        IslandChestStorage.removeBlock(first)
        second?.let { IslandChestStorage.removeBlock(it) }
        container.forEach { slot ->
            if (slot.container is Inventory) return@forEach
            if (slot.isInFirstHalf()) {
                IslandChestStorage.addItem(slot.item, slot.savableIndex, first, second)
            } else if (second != null) {
                IslandChestStorage.addItem(slot.item, slot.savableIndex, second, first)
            } else {
                SkyOcean.warn("Failed to save item ${slot.item.item.name.stripped} at position ($first, $second)")
            }
        }
        IslandChestStorage.save()
        resetCoords()
        ChestTracker.container = null
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("reset islandchests") {
            IslandChestStorage.clear()
            Text.of("Successfully cleared all island chests!").sendWithPrefix()
        }
    }

    private val Slot.savableIndex get() = this.index % 27
    private fun Slot.isInFirstHalf() = this.index < 27

    private fun getType(chestType: ChestType): DoubleBlockCombiner.BlockType = when (chestType) {
        ChestType.SINGLE -> DoubleBlockCombiner.BlockType.SINGLE
        ChestType.RIGHT -> DoubleBlockCombiner.BlockType.FIRST
        else -> DoubleBlockCombiner.BlockType.SECOND
    }

}
