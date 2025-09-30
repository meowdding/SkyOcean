package me.owdding.skyocean.mixins.features;

import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class HideFireMixin {

    @Inject(method = {"renderFlame"}, at = @At("HEAD"), cancellable = true)
    public void renderFlame(CallbackInfo ci) {
        if (MiscConfig.INSTANCE.getHideEntityFire()) {
            ci.cancel();
        }
    }

}
