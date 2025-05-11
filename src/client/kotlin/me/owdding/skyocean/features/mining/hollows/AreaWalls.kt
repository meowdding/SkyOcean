package me.owdding.skyocean.features.mining.hollows

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.events.RenderWorldEvent
import me.owdding.skyocean.utils.boundingboxes.CrystalHollowsBB
import me.owdding.skyocean.utils.rendering.RenderUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.levelgen.structure.BoundingBox
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.translated

@Module
object AreaWalls {

    private enum class Area(val color: Color, val box: BoundingBox) {
        MITHRIL(MinecraftColors.GREEN, CrystalHollowsBB.MITHRIL),
        PRECURSOR(MinecraftColors.BLUE, CrystalHollowsBB.PRECURSOR),
        JUNGLE(MinecraftColors.DARK_PURPLE, CrystalHollowsBB.JUNGLE),
        GOBLIN(MinecraftColors.GOLD, CrystalHollowsBB.GOBLIN),
        MAGMA_FIELDS(MinecraftColors.RED, CrystalHollowsBB.MAGMA_FIELDS),
        NUCLEUS(MinecraftColors.WHITE, CrystalHollowsBB.NUCLEUS);

        operator fun component1(): Color = color.withAlpha(0x9F)
        operator fun component2() = box
        operator fun contains(pos: BlockPos) = box.isInside(pos)
    }


    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onRender(event: RenderWorldEvent) {
        if (!MiningConfig.chAreaWalls) return

        event.pose.translated(-event.camera.position.x, -event.camera.position.y, -event.camera.position.z) {
            val blockPosition = event.camera.blockPosition
            when (blockPosition) {
                in Area.NUCLEUS -> renderNucleus(event)
                in Area.MAGMA_FIELDS -> renderMagmaFields(event)
                in Area.MITHRIL -> {
                    renderOutsideShape(event, Area.NUCLEUS, Direction.NORTH)
                    renderOutsideShape(event, Area.NUCLEUS, Direction.EAST)
                    renderOutsideShape(event, Area.PRECURSOR, Direction.NORTH)
                    renderOutsideShape(event, Area.JUNGLE, Direction.EAST)
                    renderMagmaFieldsTop(event)
                }

                in Area.GOBLIN -> {
                    renderOutsideShape(event, Area.NUCLEUS, Direction.WEST)
                    renderOutsideShape(event, Area.NUCLEUS, Direction.SOUTH)
                    renderOutsideShape(event, Area.PRECURSOR, Direction.WEST)
                    renderOutsideShape(event, Area.JUNGLE, Direction.SOUTH)
                    renderMagmaFieldsTop(event)
                }

                in Area.PRECURSOR -> {
                    renderOutsideShape(event, Area.NUCLEUS, Direction.SOUTH)
                    renderOutsideShape(event, Area.NUCLEUS, Direction.EAST)
                    renderOutsideShape(event, Area.MITHRIL, Direction.SOUTH)
                    renderOutsideShape(event, Area.GOBLIN, Direction.EAST)
                    renderMagmaFieldsTop(event)
                }

                in Area.JUNGLE -> {
                    renderOutsideShape(event, Area.NUCLEUS, Direction.WEST)
                    renderOutsideShape(event, Area.NUCLEUS, Direction.NORTH)
                    renderOutsideShape(event, Area.MITHRIL, Direction.WEST)
                    renderOutsideShape(event, Area.GOBLIN, Direction.NORTH)
                    renderMagmaFieldsTop(event)
                }
            }
        }
    }

    private fun renderOutsideShape(event: RenderWorldEvent, area: Area, direction: Direction) = renderOutsideBox(event, area, skipIf = { it != direction })

    private fun renderMagmaFields(event: RenderWorldEvent) {
        event.pose.translated(0, -0.01, 0) {
            renderOutsideShape(event, Area.NUCLEUS, Direction.DOWN)
        }
        renderOutsideShape(event, Area.MITHRIL, Direction.DOWN)
        renderOutsideShape(event, Area.PRECURSOR, Direction.DOWN)
        renderOutsideShape(event, Area.GOBLIN, Direction.DOWN)
        renderOutsideShape(event, Area.JUNGLE, Direction.DOWN)
    }

    private fun renderMagmaFieldsTop(event: RenderWorldEvent) {
        val (color, box) = Area.MAGMA_FIELDS
        RenderUtils.renderPlane(event, Direction.UP, box.minX(), box.minZ(), box.maxX(), box.maxZ(), box.maxY() + 1, color.value)
    }

    private fun renderNucleus(event: RenderWorldEvent) {
        renderInsideBox(event, Area.NUCLEUS, listOf(Direction.UP))
        renderMagmaFieldsTop(event)
    }

    private fun renderInsideBox(
        event: RenderWorldEvent,
        area: Area,
        sides: List<Direction> = listOf(),
        skipIf: (Direction) -> Boolean = { sides.contains(it) },
    ) {
        val (color, box) = area
        val colorValue = color.value
        if (!skipIf(Direction.DOWN)) {
            RenderUtils.renderPlane(event, Direction.DOWN, box.minX(), box.minZ(), box.maxX() + 1, box.maxZ() + 1, box.maxY() + 1, colorValue)
        }
        if (!skipIf(Direction.UP)) {
            RenderUtils.renderPlane(event, Direction.UP, box.minX(), box.minZ(), box.maxX() + 1, box.maxZ() + 1, box.minY(), colorValue)
        }
        if (!skipIf(Direction.NORTH)) {
            RenderUtils.renderPlane(event, Direction.NORTH, box.minX(), box.minY(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1, colorValue)
        }
        if (!skipIf(Direction.SOUTH)) {
            RenderUtils.renderPlane(event, Direction.SOUTH, box.minX(), box.minY(), box.maxX() + 1, box.maxY() + 1, box.minZ(), colorValue)
        }
        if (!skipIf(Direction.EAST)) {
            RenderUtils.renderPlane(event, Direction.EAST, box.minY(), box.minZ(), box.maxY() + 1, box.maxZ() + 1, box.minX(), colorValue)
        }
        if (!skipIf(Direction.WEST)) {
            RenderUtils.renderPlane(event, Direction.WEST, box.minY(), box.minZ(), box.maxY() + 1, box.maxZ() + 1, box.maxX() + 1, colorValue)
        }
    }

    private fun renderOutsideBox(
        event: RenderWorldEvent,
        area: Area,
        sides: List<Direction> = listOf(),
        skipIf: (Direction) -> Boolean = { sides.contains(it) },
    ) {
        val (color, box) = area
        val colorValue = color.value
        if (!skipIf(Direction.DOWN)) {
            RenderUtils.renderPlane(event, Direction.DOWN, box.minX(), box.minZ(), box.maxX() + 1, box.maxZ() + 1, box.minY(), colorValue)
        }
        if (!skipIf(Direction.UP)) {
            RenderUtils.renderPlane(event, Direction.UP, box.minX(), box.minZ(), box.maxX() + 1, box.maxZ() + 1, box.maxY() + 1, colorValue)
        }
        if (!skipIf(Direction.NORTH)) {
            RenderUtils.renderPlane(event, Direction.NORTH, box.minX(), box.minY(), box.maxX() + 1, box.maxY() + 1, box.minZ(), colorValue)
        }
        if (!skipIf(Direction.SOUTH)) {
            RenderUtils.renderPlane(event, Direction.SOUTH, box.minX(), box.minY(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1, colorValue)
        }
        if (!skipIf(Direction.EAST)) {
            RenderUtils.renderPlane(event, Direction.EAST, box.minY(), box.minZ(), box.maxY() + 1, box.maxZ() + 1, box.maxX() + 1, colorValue)
        }
        if (!skipIf(Direction.WEST)) {
            RenderUtils.renderPlane(event, Direction.WEST, box.minY(), box.minZ(), box.maxY() + 1, box.maxZ() + 1, box.minX(), colorValue)
        }
    }
}
