package me.owdding.skyocean.features.garden.mutations

import me.owdding.ktmodules.Module
import me.owdding.lib.platform.screens.MeowddingScreen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.core.Vec3i
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.FarmBlock
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient

class BlockRenderTestScreen : MeowddingScreen() {

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

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
        super.render(graphics, mouseX, mouseY, f)

        val bounds = ScreenRectangle(0, 0, width, height).transformMaxBounds(graphics.pose())
        val blocks = mapOf(
            Vec3i(0, 0, 0) to Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE),
            Vec3i(1, 0, 0) to Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE),
            Vec3i(0, 0, 1) to Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE),
            Vec3i(-1, 0, 0) to Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE),
            Vec3i(0, 0, -1) to Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE),
            Vec3i(1, 1, 0) to Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE),
            Vec3i(0, 1, 1) to Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE),
            Vec3i(-1, 1, 0) to Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE),
            Vec3i(0, 1, -1) to Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE),
        )
        graphics.guiRenderState.submitPicturesInPictureState(
            GuiBlockRenderState(
                blocks,
                Vec3(xAngle, yAngle, 0.0),
                scale,
                bounds,
                graphics.scissorStack.peek(),
                graphics.pose(),
            )
        )
    }

    @Module
    companion object {
        @Subscription
        fun command(event: RegisterCommandsEvent) {
            event.registerWithCallback("tot") {
                McClient.setScreenAsync {
                    BlockRenderTestScreen()
                }
            }
        }
    }
}
