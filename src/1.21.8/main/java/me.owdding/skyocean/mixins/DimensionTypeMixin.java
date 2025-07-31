package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {

    @ModifyReturnValue(method = "cloudHeight", at = @At("RETURN"))
    private Optional<Integer> getCloudHeight(Optional<Integer> original) {
        if (MiscConfig.INSTANCE.getShouldHideClouds()) {
            return Optional.empty();
        }
        return original;
    }

}
