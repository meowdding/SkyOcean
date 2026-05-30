package me.owdding.skyocean.mixins.features.customize;

import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
/*? if >=26.1 {*/
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/*?} else {*/
/*import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
 *//*?}*/
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland;

@Mixin(FogRenderer.class)
public class NetherFogColorMixin {

    /*? if >=26.1 {*/
    @Inject(method = "computeFogColor", at = @At("RETURN"))
    private void skyocean$darkenNetherFog(
        Camera camera,
        float partialTicks,
        ClientLevel clientLevel,
        int renderDistance,
        float darkenWorldAmount,
        Vector4f color,
        CallbackInfo ci
    ) {
        if (camera.getFluidInCamera() != FogType.NONE) return; // skips lava fog
        if (!SkyBlockIsland.CRIMSON_ISLE.inIsland()) return;
        if (!MiscConfig.INSTANCE.getNetherFogDarkening()) return;
        if (Minecraft.getInstance().player == null) return;
        if (!Minecraft.getInstance().player.hasEffect(MobEffects.NIGHT_VISION)) return;
        color.set(color.x * 0.20f, color.y * 0.20f, color.z * 0.20f, color.w);
    }
    /*?} else {*/
    /*@Inject(method = "computeFogColor", at = @At("RETURN"), cancellable = true)
    private void skyocean$darkenNetherFog(
        Camera camera,
        float partialTicks,
        ClientLevel clientLevel,
        int renderDistance,
        float darkenWorldAmount,
        CallbackInfoReturnable<Vector4f> cir
    ) {
        if (camera.getFluidInCamera() != FogType.NONE) return; // skips lava fog
        if (!SkyBlockIsland.CRIMSON_ISLE.inIsland()) return;
        if (!MiscConfig.INSTANCE.getNetherFogDarkening()) return;
        if (Minecraft.getInstance().player == null) return;
        if (!Minecraft.getInstance().player.hasEffect(MobEffects.NIGHT_VISION)) return;
        Vector4f color = cir.getReturnValue();
        cir.setReturnValue(new Vector4f(color.x * 0.20f, color.y * 0.20f, color.z * 0.20f, color.w));
    }
    *//*?}*/
}
