package me.owdding.skyocean.mixins.features;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Options.class)
public class OptionsMixin {

    @WrapMethod(method = "getCloudsType")
    private CloudStatus modifyCloudsType(Operation<CloudStatus> original) {
        if (MiscConfig.INSTANCE.getShouldHideClouds()) {
            return CloudStatus.OFF;
        }
        return original.call();
    }

}
