package me.owdding.skyocean.utils.rendering

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

actual fun GuiGraphics.applyPostEffect(id: ResourceLocation) {
    this.nextStratum()
    (this.guiRenderState as? PostEffectApplicator)?.`skyocean$applyPostEffect`(id)
    this.fill(0, 0, this.guiWidth(), this.guiHeight(), 0)
}
