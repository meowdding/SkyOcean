package me.owdding.skyocean.features.mining.hollows

import com.teamresourceful.resourcefullib.common.color.Color
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.utils.boundingboxes.CrystalHollowsBB
import me.owdding.skyocean.utils.extensions.toBlockPos
import me.owdding.skyocean.utils.rendering.RenderUtils.renderPlane
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.levelgen.structure.BoundingBox
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
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
        NUCLEUS(MinecraftColors.WHITE, CrystalHollowsBB.NUCLEUS),
        ;

        operator fun component1(): Color = color.withAlpha(0x9F)
        operator fun component2() = box
        operator fun contains(pos: BlockPos) = box.isInside(pos)
    }


    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    private fun RenderWorldEvent.AfterTranslucent.onRender() {
        if (!MiningConfig.chAreaWalls) return

        atCamera {
            val blockPosition = cameraPosition.toBlockPos()
            when (blockPosition) {
                in Area.NUCLEUS -> renderNucleus()
                in Area.MAGMA_FIELDS -> renderMagmaFields()
                in Area.MITHRIL -> {
                    renderOutsideShape(Area.NUCLEUS, Direction.NORTH)
                    renderOutsideShape(Area.NUCLEUS, Direction.EAST)
                    renderOutsideShape(Area.PRECURSOR, Direction.NORTH)
                    renderOutsideShape(Area.JUNGLE, Direction.EAST)
                    renderMagmaFieldsTop()
                }

                in Area.GOBLIN -> {
                    renderOutsideShape(Area.NUCLEUS, Direction.WEST)
                    renderOutsideShape(Area.NUCLEUS, Direction.SOUTH)
                    renderOutsideShape(Area.PRECURSOR, Direction.WEST)
                    renderOutsideShape(Area.JUNGLE, Direction.SOUTH)
                    renderMagmaFieldsTop()
                }

                in Area.PRECURSOR -> {
                    renderOutsideShape(Area.NUCLEUS, Direction.SOUTH)
                    renderOutsideShape(Area.NUCLEUS, Direction.EAST)
                    renderOutsideShape(Area.MITHRIL, Direction.SOUTH)
                    renderOutsideShape(Area.GOBLIN, Direction.EAST)
                    renderMagmaFieldsTop()
                }

                in Area.JUNGLE -> {
                    renderOutsideShape(Area.NUCLEUS, Direction.WEST)
                    renderOutsideShape(Area.NUCLEUS, Direction.NORTH)
                    renderOutsideShape(Area.MITHRIL, Direction.WEST)
                    renderOutsideShape(Area.GOBLIN, Direction.NORTH)
                    renderMagmaFieldsTop()
                }
            }
        }
    }

    private fun RenderWorldEvent.renderOutsideShape(area: Area, direction: Direction) = renderOutsideBox(area, skipIf = { it != direction })

    private fun RenderWorldEvent.renderMagmaFields() {
        poseStack.translated(0, -0.01, 0) {
            renderOutsideShape(Area.NUCLEUS, Direction.DOWN)
        }
        renderOutsideShape(Area.MITHRIL, Direction.DOWN)
        renderOutsideShape(Area.PRECURSOR, Direction.DOWN)
        renderOutsideShape(Area.GOBLIN, Direction.DOWN)
        renderOutsideShape(Area.JUNGLE, Direction.DOWN)
    }

    private fun RenderWorldEvent.renderMagmaFieldsTop() {
        val (color, box) = Area.MAGMA_FIELDS
        renderPlane(Direction.UP, box.minX(), box.minZ(), box.maxX(), box.maxZ(), box.maxY() + 1, color.value)
    }

    private fun RenderWorldEvent.renderNucleus() {
        renderInsideBox(Area.NUCLEUS, listOf(Direction.UP))
        renderMagmaFieldsTop()
    }

    private fun RenderWorldEvent.renderInsideBox(
        area: Area,
        sides: List<Direction> = listOf(),
        skipIf: (Direction) -> Boolean = { sides.contains(it) },
    ) {
        val (color, box) = area
        val colorValue = color.value
        if (!skipIf(Direction.DOWN)) {
            renderPlane(Direction.DOWN, box.minX(), box.minZ(), box.maxX() + 1, box.maxZ() + 1, box.maxY() + 1, colorValue)
        }
        if (!skipIf(Direction.UP)) {
            renderPlane(Direction.UP, box.minX(), box.minZ(), box.maxX() + 1, box.maxZ() + 1, box.minY(), colorValue)
        }
        if (!skipIf(Direction.NORTH)) {
            renderPlane(Direction.NORTH, box.minX(), box.minY(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1, colorValue)
        }
        if (!skipIf(Direction.SOUTH)) {
            renderPlane(Direction.SOUTH, box.minX(), box.minY(), box.maxX() + 1, box.maxY() + 1, box.minZ(), colorValue)
        }
        if (!skipIf(Direction.EAST)) {
            renderPlane(Direction.EAST, box.minY(), box.minZ(), box.maxY() + 1, box.maxZ() + 1, box.minX(), colorValue)
        }
        if (!skipIf(Direction.WEST)) {
            renderPlane(Direction.WEST, box.minY(), box.minZ(), box.maxY() + 1, box.maxZ() + 1, box.maxX() + 1, colorValue)
        }
    }

    private fun RenderWorldEvent.renderOutsideBox(
        area: Area,
        sides: List<Direction> = listOf(),
        skipIf: (Direction) -> Boolean = { sides.contains(it) },
    ) {
        val (color, box) = area
        val colorValue = color.value
        if (!skipIf(Direction.DOWN)) {
            renderPlane(Direction.DOWN, box.minX(), box.minZ(), box.maxX() + 1, box.maxZ() + 1, box.minY(), colorValue)
        }
        if (!skipIf(Direction.UP)) {
            renderPlane(Direction.UP, box.minX(), box.minZ(), box.maxX() + 1, box.maxZ() + 1, box.maxY() + 1, colorValue)
        }
        if (!skipIf(Direction.NORTH)) {
            renderPlane(Direction.NORTH, box.minX(), box.minY(), box.maxX() + 1, box.maxY() + 1, box.minZ(), colorValue)
        }
        if (!skipIf(Direction.SOUTH)) {
            renderPlane(Direction.SOUTH, box.minX(), box.minY(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1, colorValue)
        }
        if (!skipIf(Direction.EAST)) {
            renderPlane(Direction.EAST, box.minY(), box.minZ(), box.maxY() + 1, box.maxZ() + 1, box.maxX() + 1, colorValue)
        }
        if (!skipIf(Direction.WEST)) {
            renderPlane(Direction.WEST, box.minY(), box.minZ(), box.maxY() + 1, box.maxZ() + 1, box.minX(), colorValue)
        }
    }
}
