package me.owdding.skyocean.mixins.features.fishing;

import me.owdding.skyocean.features.fishing.LavaReplacement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(FluidStateModelSet.class)
public class FluidStateModelSetMixin {

    @Inject(method = "bake", at = @At("RETURN"))
    private static void skyocean$bakeOpaqueWater(CallbackInfoReturnable<Map<Fluid, FluidModel>> cir) {
        LavaReplacement.onModelsBaked(cir.getReturnValue());
    }

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private FluidModel skyocean$replaceLava(FluidModel original, FluidState state) {
        return LavaReplacement.getReplacementModel(state, original);
    }
}
