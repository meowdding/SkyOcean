package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.owdding.skyocean.helpers.HeadLayerAlphaHolder;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {

    @WrapOperation(
        method = "renderSkull",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/SkullModelBase;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
        )
    )
    private static void renderSkull(
        SkullModelBase instance,
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        int packedLight,
        int packedOverlay,
        Operation<Void> original
    ) {
        if (HeadLayerAlphaHolder.alpha != null) {
            instance.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, (HeadLayerAlphaHolder.alpha << 24) | 0xFFFFFF);
            return;
        }

        original.call(instance, poseStack, vertexConsumer, packedLight, packedOverlay);
    }

}
