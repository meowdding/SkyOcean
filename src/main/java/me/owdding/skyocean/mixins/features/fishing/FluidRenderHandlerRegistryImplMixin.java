package me.owdding.skyocean.mixins.features.fishing;

//? if < 26.1 {
/*import me.owdding.skyocean.features.fishing.LavaReplacement;
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

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = FluidRenderHandlerRegistryImpl.class, remap = false)
public class FluidRenderHandlerRegistryImplMixin {

    @Shadow
    @Final
    private Map<Fluid, FluidRenderHandler> handlers;

    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private void skyocean$replaceLava(Fluid fluid, CallbackInfoReturnable<FluidRenderHandler> cir) {
        if (!LavaReplacement.INSTANCE.isActive()) return;

        if (fluid == Fluids.LAVA) cir.setReturnValue(handlers.get(LavaReplacement.OPAQUE_WATER));
        else if (fluid == Fluids.FLOWING_LAVA) cir.setReturnValue(handlers.get(LavaReplacement.OPAQUE_FLOWING_WATER));
    }
}
*///? }
