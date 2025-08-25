package me.owdding.skyocean.utils.rendering

import me.owdding.skyocean.mixins.GameRendererAccessor
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.LevelTargetBundle
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.helpers.McClient

actual fun GuiGraphics.applyPostEffect(id: ResourceLocation) {
    val mc = McClient.self
    val pool = (mc.gameRenderer as GameRendererAccessor).resourcePool
    val shaders = mc.shaderManager
    shaders.getPostChain(id, LevelTargetBundle.MAIN_TARGETS)?.process(mc.mainRenderTarget, pool) {}
}
