package me.owdding.skyocean.utils.rendering

import com.mojang.authlib.GameProfile
import com.mojang.math.Axis
import com.teamresourceful.resourcefullib.client.screens.CursorScreen
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.ui.UIConstants
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.player.RemotePlayer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.platform.showTooltip
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.util.*

private const val BUTTON_SIZE = 5

actual class StyledItemWidget actual constructor(val stack: ItemStack) : BaseWidget() {

    private val AUTO_ROTATE_ICON = WidgetSprites(SkyOcean.id("auto_rotate"), SkyOcean.id("auto_rotate_hovered"))

    private var isAutoRotating = true
    private var rotation: Float = 0f
        get() = if (isAutoRotating) {
            45f + (System.currentTimeMillis() / 20) % 360
        } else {
            field
        }

    private val entity = RemotePlayer(Minecraft.getInstance().level, GameProfile(UUID.randomUUID(), "Item Preview"))


    private val buttonX get() = this.x + 2
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
                InventoryScreen.renderEntityInInventory(
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
//                 val pose = graphics.pose()
//                 pose.pushPose()
//                 pose.translate(this.x + width / 2f, this.y + height / 2f + 10f, 150f)
//                 pose.mulPose(Matrix4f().scaling(1.0F, -1.0F, 1.0F))
//                 pose.scale(40f, 40f, 40f)
//                 pose.translate(0f, 0.25f, 0f)
//                 pose.mulPose(Axis.YN.rotationDegrees(this.rotation))
//
//                 graphics.drawSpecial { buffer ->
//                     McClient.self.itemRenderer.renderStatic(
//                         null,
//                         this.stack,
//                         ItemDisplayContext.NONE,
//                         pose,
//                         buffer,
//                         McLevel.self,
//                         LightTexture.FULL_BRIGHT,
//                         OverlayTexture.NO_OVERLAY,
//                         0,
//                     )
//                 }
//
//                 pose.popPose()
            }
        }

        this.isButtonHovered = isMouseOverButton(mouseX, mouseY)
        if (!this.isAutoRotating) {
            graphics.drawSprite(AUTO_ROTATE_ICON.get(false, this.isButtonHovered), buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE)
            if (this.isButtonHovered) {
                graphics.showTooltip(Text.of("Enable Auto Rotate"), mouseX, mouseY)
            }
        }
    }

    override fun onDrag(mouseX: Double, mouseY: Double, dragX: Double, dragY: Double) {
        this.isAutoRotating = false
        this.rotation -= dragX.toFloat() * 3
        this.rotation = (this.rotation + 360) % 360
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (isMouseOverButton(mouseX.toInt(), mouseY.toInt()) && !this.isAutoRotating) {
            McClient.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.25f)
            this.isAutoRotating = true
        }
    }

    override fun getCursor(): CursorScreen.Cursor? = when {
        this.isButtonHovered -> CursorScreen.Cursor.POINTER
        this.isHovered -> CursorScreen.Cursor.RESIZE_EW
        else -> super.cursor
    }

    private fun isMouseOverButton(mouseX: Int, mouseY: Int): Boolean {
        return mouseX in buttonX until (buttonX + BUTTON_SIZE) && mouseY in buttonY until (buttonY + BUTTON_SIZE)
    }
}
