package me.owdding.skyocean.mixins.features.fishing;

import me.owdding.skyocean.config.features.fishing.FishingConfig;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
//~ if >= 26.1 'FluidRenderHandlerRegistryImpl' -> 'FluidRenderingRegistryImpl'
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingRegistryImpl;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
//~ if >= 26.1 'FluidRenderHandlerRegistryImpl' -> 'FluidRenderingRegistryImpl'
@Mixin(value = FluidRenderingRegistryImpl.class, remap = false)
public class FluidRenderHandlerRegistryImplMixin {

    @Shadow
    @Final
    //~ if >= 26.1 'handlers' -> 'HANDLERS' {
    //~ if >= 26.1 'private' -> 'private static' {
    private static Map<Fluid, FluidRenderHandler> HANDLERS;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void skyocean$redirectLavaRendering(Fluid fluid, CallbackInfoReturnable<FluidRenderHandler> cir) {
        if (!SkyBlockIsland.CRIMSON_ISLE.inIsland()) {
            return;
        }
        if (!FishingConfig.INSTANCE.getLavaReplacement()) return;
        if (fluid == Fluids.LAVA) cir.setReturnValue(HANDLERS.get(Fluids.WATER));
        else if (fluid == Fluids.FLOWING_LAVA) cir.setReturnValue(HANDLERS.get(Fluids.FLOWING_WATER));
    }
    //~ }
    //~ }

}
