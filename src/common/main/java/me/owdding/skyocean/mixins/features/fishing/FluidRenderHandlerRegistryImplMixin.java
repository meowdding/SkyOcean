package me.owdding.skyocean.mixins.features.fishing;

import me.owdding.skyocean.config.features.fishing.FishingConfig;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
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
@Mixin(value = FluidRenderHandlerRegistryImpl.class, remap = false)
public class FluidRenderHandlerRegistryImplMixin {

    @Shadow
    @Final
    private Map<Fluid, FluidRenderHandler> handlers;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void skyocean$redirectLavaRendering(Fluid fluid, CallbackInfoReturnable<FluidRenderHandler> cir) {
        if (!SkyBlockIsland.CRIMSON_ISLE.inIsland()) return;
        if (!FishingConfig.INSTANCE.getLavaReplacement()) return;
        if (fluid == Fluids.LAVA) cir.setReturnValue(handlers.get(Fluids.WATER));
        else if (fluid == Fluids.FLOWING_LAVA) cir.setReturnValue(handlers.get(Fluids.FLOWING_WATER));
    }

}
