package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DimensionSpecialEffects.class)
public class DimensionSpecialEffectsMixin {

    @ModifyReturnValue(method = "getCloudHeight", at = @At("RETURN"))
    private float getCloudHeight(float original) {
        if (MiscConfig.INSTANCE.getShouldHideClouds()) {
            return Float.NaN;
        }
        return original;
    }

}
