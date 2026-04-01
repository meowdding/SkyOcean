package me.owdding.skyocean.mixins.features.hidelightning;

import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBoltRenderer.class)
public class LightningBoltRendererMixin {

    @Inject(
        //~ if >= 26.1 'CameraRenderState' -> 'level/CameraRenderState'
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LightningBoltRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void renderLightningBolt(CallbackInfo ci) {
        if (MiscConfig.INSTANCE.getHideLightning()) {
            ci.cancel();
        }
    }
}
