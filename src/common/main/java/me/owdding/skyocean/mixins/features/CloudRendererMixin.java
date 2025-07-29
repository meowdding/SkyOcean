package me.owdding.skyocean.mixins.features;

import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public class CloudRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(CallbackInfo ci) {
        if (MiscConfig.INSTANCE.getShouldHideClouds()) {
            ci.cancel();
        }
    }

}
