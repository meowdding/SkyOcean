package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.teamresourceful.resourcefullib.client.screens.CursorScreen
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.ui.UIConstants
import me.owdding.lib.rendering.MeowddingPipState
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.item.TrackingItemStackRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Matrix3x2f
import org.joml.Quaternionf
import org.joml.Vector3f
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.platform.showTooltip
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.util.function.Function


private const val BUTTON_SIZE = 5

data class ItemWidgetItemState(
    override val x0: Int,
    override val y0: Int,
    override val x1: Int,
    override val y1: Int,
    override val scissorArea: ScreenRectangle?,
    override val pose: Matrix3x2f,
    val rotation: Float,
    val item: TrackingItemStackRenderState,
) : MeowddingPipState<ItemWidgetItemState>() {
    override val shrinkToScissor: Boolean = false

    override fun getFactory(): Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<ItemWidgetItemState>> =
        Function { buffer -> ItemWidgetRenderer(buffer) }
}

class ItemWidgetRenderer(source: MultiBufferSource.BufferSource) : PictureInPictureRenderer<ItemWidgetItemState>(source) {

    override fun getRenderStateClass(): Class<ItemWidgetItemState> = ItemWidgetItemState::class.java
    override fun getTextureLabel(): String = "skyocean_item_widget"

    override fun renderToTexture(state: ItemWidgetItemState, stack: PoseStack) {
        val bounds = state.bounds ?: return

        stack.pushPose()
        stack.translate(0f, bounds.height() / -2f - 5f, 0f)
        stack.scale(40f, 40f, 40f)
        stack.mulPose(Axis.ZN.rotationDegrees(180f))
        stack.mulPose(Axis.YN.rotationDegrees(state.rotation))

        McClient.self.gameRenderer.lighting.setupFor(if (state.item.usesBlockLight()) Lighting.Entry.ITEMS_3D else Lighting.Entry.ITEMS_FLAT)

        state.item.submit(
            stack,
            McClient.self.gameRenderer.featureRenderDispatcher.submitNodeStorage,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY,
            0,
        )

        stack.popPose()
    }
}


class StyledItemWidget(val stack: ItemStack) : BaseWidget() {

    private val autoRotateIcon = WidgetSprites(SkyOcean.id("auto_rotate"), SkyOcean.id("auto_rotate_disabled"), SkyOcean.id("auto_rotate_hovered"))
    private val leftArrowIcon = SkyOcean.id("left_arrow")
    private val rightArrowIcon = SkyOcean.id("right_arrow")

    private var isAutoRotating = true
    private var rotation: Float = 0f
        get() = if (isAutoRotating) {
            45f + (System.currentTimeMillis() / 20) % 360
        } else {
            field
        }

    private val entity = ArmorStand(McClient.self.level!!, 0.0, 0.0, 0.0)
    private val buttonX get() = this.x + (this.width - BUTTON_SIZE) / 2
    private val buttonY get() = this.y + this.height - BUTTON_SIZE - 2

    private var isButtonHovered = false

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        graphics.drawSprite(UIConstants.MODAL_INSET, x, y, width, height)

        if (this.stack.isEmpty) return

        val slot = this.stack[DataComponents.EQUIPPABLE]?.slot?.takeIf(EquipmentSlot::isArmor)

        graphics.scissor(this.x..this.x + this.width, this.y..this.y + this.height) {
            if (slot != null) {
                this.entity.setItemSlot(slot, this.stack)
                this.entity.isInvisible = true

                val angle = Quaternionf().rotateYXZ(rotation * 0.017453292f, 180 * 0.017453292f, 0f)
                renderEntityInInventory(
                    graphics,
                    x,
                    y,
                    x + width,
                    y + height,
                    25f,
                    Vector3f(0f, 1f, 0f),
                    angle,
                    null,
                    this.entity,
                )
            } else {
                val itemState = TrackingItemStackRenderState()
                McClient.self.itemModelResolver.updateForTopItem(itemState, this.stack, ItemDisplayContext.NONE, McLevel.self, null, 0)
                graphics.guiRenderState.submitPicturesInPictureState(
                    ItemWidgetItemState(
                        x, y, x + width, y + height,
                        graphics.scissorStack.peek(),
                        Matrix3x2f(graphics.pose()),
                        this.rotation,
                        itemState,
                    ),
                )
            }
        }

        this.isButtonHovered = isMouseOverButton(mouseX, mouseY)
        graphics.drawSprite(leftArrowIcon, buttonX - 1 - 7, buttonY, 7, BUTTON_SIZE)
        graphics.drawSprite(
            autoRotateIcon.get(!this.isAutoRotating, !this.isAutoRotating && this.isButtonHovered),
            buttonX,
            buttonY,
            BUTTON_SIZE,
            BUTTON_SIZE,
        )
        graphics.drawSprite(rightArrowIcon, buttonX + BUTTON_SIZE + 1, buttonY, 7, BUTTON_SIZE)
        if (!this.isAutoRotating && this.isButtonHovered) {
            graphics.showTooltip(Text.of("Enable Auto Rotate"), mouseX, mouseY)
        }
    }

    override fun onDrag(event: MouseButtonEvent, deltaX: Double, deltaY: Double) {
        this.isAutoRotating = false
        this.rotation -= deltaX.toFloat() * 3
        this.rotation = (this.rotation + 360) % 360
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        val mouseX = event.x
        val mouseY = event.y
        if (isMouseOverButton(mouseX.toInt(), mouseY.toInt()) && !this.isAutoRotating) {
            McClient.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.25f)
            this.isAutoRotating = true
        }
    }

    override fun getCursor(): CursorScreen.Cursor? = when {
        this.isButtonHovered && !this.isAutoRotating -> CursorScreen.Cursor.POINTER
        this.isHovered -> CursorScreen.Cursor.RESIZE_EW
        else -> super.cursor
    }

    private fun isMouseOverButton(mouseX: Int, mouseY: Int): Boolean = mouseX in buttonX until (buttonX + BUTTON_SIZE) &&
        mouseY in buttonY until (buttonY + BUTTON_SIZE)
}

@Suppress("SameParameterValue")
private fun renderEntityInInventory(
    graphics: GuiGraphics,

    x0: Int,
    y0: Int,
    width: Int,
    height: Int,
    scale: Float,
    translation: Vector3f,
    rotation: Quaternionf,
    overrideCameraAngle: Quaternionf?,
    entity: LivingEntity,
) {
    //? if < 1.21.11 {
    /*InventoryScreen.renderEntityInInventory(graphics, x0, y0, width, height, scale, translation, rotation, overrideCameraAngle, entity)
   *///?} else {
    val renderState = InventoryScreen.extractRenderState(entity)
    graphics.submitEntityRenderState(renderState, scale, translation, rotation, overrideCameraAngle, x0, y0, width, height)
    //?}

}
