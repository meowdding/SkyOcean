package me.owdding.skyocean.mixins.features;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public class OptionsMixin {

    @ModifyReturnValue(method = "getCloudsType", at = @At("RETURN"))
    private CloudStatus modifyCloudsType(CloudStatus original) {
        if (MiscConfig.INSTANCE.getShouldHideClouds()) {
            return CloudStatus.OFF;
        }
        return original;
    }

}
