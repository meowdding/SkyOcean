package me.owdding.skyocean.mixins.features.item_outlines;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import me.owdding.skyocean.features.item.RarityOutlines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Inject(
        method = "bindDefaultUniforms",
        at = @At(
            value = "TAIL"
        )
    )
    private static void onBindDefault(RenderPass renderPass, CallbackInfo ci) {
        GpuBuffer buffer = RarityOutlines.Buffer.getGpuBuffer();
        if (buffer != null) {
            renderPass.setUniform(RarityOutlines.Buffer.NAME, buffer);
        }
    }
}
