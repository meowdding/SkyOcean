package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.utils.rendering.PostEffectApplicator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Unique
    private ResourceLocation skyocean$currentEffect = null;

    @Shadow
    @Final
    GuiRenderState renderState;

    @Inject(method = "prepare", at = @At("TAIL"))
    private void skyocean$resetPostEffect(CallbackInfo ci) {
        this.skyocean$currentEffect = this.renderState instanceof PostEffectApplicator applicator ? applicator.skyocean$getPostEffect() : null;
    }

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;processBlurEffect()V"))
    private void skyocean$applyPostEffectIf(GameRenderer instance, Operation<Void> original) {
        if (this.skyocean$currentEffect != null && instance instanceof GameRendererAccessor accessor) {
            var mc = Minecraft.getInstance();
            var chain = mc.getShaderManager().getPostChain(this.skyocean$currentEffect, LevelTargetBundle.MAIN_TARGETS);
            if (chain != null) {
                chain.process(mc.getMainRenderTarget(), accessor.getResourcePool());
                return;
            }
        }
        original.call(instance);
    }
}
