package me.owdding.skyocean.features.garden

import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.platform.screens.MeowddingScreen
import me.owdding.lib.utils.suggestions.IterableSuggestionProvider
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.repo.mutation.BlockSupplier
import me.owdding.skyocean.repo.mutation.MutationBlueprint
import me.owdding.skyocean.repo.mutation.MutationData
import me.owdding.skyocean.repo.mutation.MutationEntry
import me.owdding.skyocean.utils.rendering.GuiBlockRenderState
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import org.joml.Vector3i
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent.Companion.argument
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.abs

class MutationViewerScreen(val entry: MutationEntry, val blueprint: MutationBlueprint) : MeowddingScreen() {
    private var yAngle: Double = 45.0
    private var xAngle: Double = 22.5
    private var scale: Float = 6f

    private val floor: Map<Vector3i, BlockSupplier> = buildMap {
        val min = blueprint.min.add(-1, -1, -1)
        val max = blueprint.max.add(1, -1, 1)
        for (x in min.x()..max.x()) {
            for (z in min.z()..max.z()) {
                val pos = Vector3i(x, min.y(), z)
                val block = Blocks.WHITE_CONCRETE.defaultBlockState().takeIf { (abs(x) + abs(z)) % 2 == 0 } ?: Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState()
                put(pos, BlockSupplier.SingleBlockSupplier(block))
            }
        }
    }


    override fun mouseDragged(event: MouseButtonEvent, deltaX: Double, deltaY: Double): Boolean {
        xAngle = (xAngle + deltaY).coerceIn(0.0, 60.0)
        yAngle += deltaX
        return super.mouseDragged(event, deltaX, deltaY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        scale = (scale + scrollY.toFloat()).coerceAtLeast(2f)
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun tick() {
        super.tick()
        blueprint.tick()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
        val bounds = ScreenRectangle(0, 0, width, height).transformMaxBounds(graphics.pose())
        graphics.guiRenderState.submitPicturesInPictureState(
            GuiBlockRenderState(
                blueprint.map + floor,
                Vec3(xAngle, yAngle, 0.0),
                scale,
                bounds,
                graphics.scissorStack.peek(),
                graphics.pose(),
            ),
        )

        DisplayFactory.vertical(3, Alignment.CENTER) {
            string("Blueprint Preview") {
                color = TextColor.WHITE
            }
            string(entry.name) {
                color = entry.rarity.color
            }
        }.apply {
            this.render(graphics, this@MutationViewerScreen.width / 2 - this.getWidth() / 2, 20)
        }

        super.render(graphics, mouseX, mouseY, f)
    }

    @Module
    companion object {
        @Subscription
        fun command(event: RegisterSkyOceanCommandEvent) {
            event.register("mutation") {
                thenCallback("name", StringArgumentType.word(), IterableSuggestionProvider(MutationData.mutations.map { it.name })) {
                    val name = argument<String>("name")
                    val mutation = MutationData.mutations.find { it.name == name }!!
                    McClient.setScreenAsync { MutationViewerScreen(mutation, mutation.blueprint!!) }
                }
            }
        }
    }
}
