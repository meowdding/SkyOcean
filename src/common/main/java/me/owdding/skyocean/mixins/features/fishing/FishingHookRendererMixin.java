package me.owdding.skyocean.mixins.features.fishing;

import com.llamalad7.mixinextras.sugar.Local;
import me.owdding.skyocean.config.features.fishing.FishingConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI;

@Mixin(FishingHookRenderer.class)
public class FishingHookRendererMixin {

    @Unique
    private static boolean isEnabled() {
        return LocationAPI.INSTANCE.isOnSkyBlock() && FishingConfig.INSTANCE.getHideOtherBobbers();
    }

    @Inject(
        method = "shouldRender(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void skyocean$hideOtherBobbers(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) FishingHook fishingHook) {
        if (isEnabled() && !(fishingHook.getPlayerOwner() instanceof LocalPlayer)) {
            cir.setReturnValue(false);
        }
    }

}
