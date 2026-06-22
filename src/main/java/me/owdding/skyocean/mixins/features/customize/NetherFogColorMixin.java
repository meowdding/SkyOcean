package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/*? if >=26.1 {*/
/*?} else {*/
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
 *//*?}*/
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland;

@Mixin(FogRenderer.class)
public class NetherFogColorMixin {

    @WrapOperation(
        method = "computeFogColor",
        at = @At(
            value = "INVOKE",
            //~ if >= 26.2 'getNightVisionScale' -> 'nightVisionScale'
            target = "Lnet/minecraft/client/renderer/GameRenderer;nightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F"
        )
    )
    private float skyocean$darkenNetherFog(
        LivingEntity livingEntity,
        @SuppressWarnings("NameDoesntMatchTargetClass") float partialTicks,
        Operation<Float> original
    ) {
        if (!MiscConfig.INSTANCE.getNetherFogDarkening()
            || !SkyBlockIsland.CRIMSON_ISLE.inIsland()
            || !(livingEntity instanceof LocalPlayer)) {
            return original.call(livingEntity, partialTicks);
        }

        return MiscConfig.INSTANCE.getNetherFogScale();
    }
}
