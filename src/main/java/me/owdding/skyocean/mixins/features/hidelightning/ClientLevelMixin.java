package me.owdding.skyocean.mixins.features.hidelightning;

import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "setSkyFlashTime", at = @At("HEAD"), cancellable = true)
    private void setSkyFlashTime(CallbackInfo ci) {
        if (MiscConfig.INSTANCE.getHideLightning()) {
            ci.cancel();
        }
    }
}
