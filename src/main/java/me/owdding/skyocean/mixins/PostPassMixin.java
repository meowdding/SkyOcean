package me.owdding.skyocean.mixins;

import me.owdding.skyocean.features.garden.cropfever.CropFeverEffects;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Supplier;

@Mixin(PostPass.class)
public class PostPassMixin {
    @ModifyArgs(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/GpuDevice;createBuffer(Ljava/util/function/Supplier;ILjava/nio/ByteBuffer;)Lcom/mojang/blaze3d/buffers/GpuBuffer;")
    )
    private void skyocean$enableCopyDst(Args args){
        Supplier<String> nameSupplier = args.get(0);
        String bufferName = nameSupplier.get();

        if (bufferName.contains(CropFeverEffects.UNIFORM_ID)) {
            int originalFlags = args.get(1);
            int copyDstFlag = 8;
            args.set(1, originalFlags | copyDstFlag);
        }
    }
}
