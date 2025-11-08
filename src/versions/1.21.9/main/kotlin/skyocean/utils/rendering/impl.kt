@file:Suppress("ACTUAL_WITHOUT_EXPECT")

package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.vertex.PoseStack
import me.owdding.lib.rendering.world.RenderTypes
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ARGB

actual fun GuiGraphics.applyPostEffect(id: ResourceLocation) {
    this.nextStratum()
    (this.guiRenderState as? PostEffectApplicator)?.`skyocean$applyPostEffect`(id)
    this.fill(0, 0, this.guiWidth(), this.guiHeight(), 0)
}

internal actual fun renderFace(
    poseStack: PoseStack,
    buffer: MultiBufferSource,
    direction: Direction,
    vec6: RenderUtils.Vec6f,
    color: Int,
) {
    ShapeRenderer.renderFace(
        poseStack.last().pose(),
        buffer.getBuffer(RenderTypes.BLOCK_FILL_QUAD),
        direction,
        vec6.a,
        vec6.b,
        vec6.c,
        vec6.d,
        vec6.e,
        vec6.f,
        ARGB.redFloat(color),
        ARGB.greenFloat(color),
        ARGB.blueFloat(color),
        ARGB.alphaFloat(color),
    )
}
