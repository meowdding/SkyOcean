package me.owdding.skyocean.features.garden.mutations

import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktmodules.Module
import me.owdding.lib.platform.screens.MeowddingScreen
import me.owdding.lib.utils.suggestions.IterableSuggestionProvider
import me.owdding.skyocean.repo.mutation.MutationBlueprint
import me.owdding.skyocean.repo.mutation.MutationData
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent.Companion.argument
import tech.thatgravyboat.skyblockapi.helpers.McClient

class BlockRenderTestScreen(val blueprint: MutationBlueprint) : MeowddingScreen() {

    private var yAngle: Double = 45.0
    private var xAngle: Double = 22.5
    private var scale: Float = 6f

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
        super.render(graphics, mouseX, mouseY, f)

        val bounds = ScreenRectangle(0, 0, width, height).transformMaxBounds(graphics.pose())
        graphics.guiRenderState.submitPicturesInPictureState(
            GuiBlockRenderState(
                blueprint.map,
                Vec3(xAngle, yAngle, 0.0),
                scale,
                bounds,
                graphics.scissorStack.peek(),
                graphics.pose(),
            ),
        )
    }

    @Module
    companion object {
        @Subscription
        fun command(event: RegisterCommandsEvent) {
            event.register("tot") {
                thenCallback("name", StringArgumentType.word(), IterableSuggestionProvider(MutationData.mutations.map { it.name })) {
                    val name = argument<String>("name")
                    val mutation = MutationData.mutations.find { it.name == name }!!
                    McClient.setScreenAsync { BlockRenderTestScreen(mutation.blueprint!!) }
                }
            }
        }
    }
}
