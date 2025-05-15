package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.owdding.skyocean.helpers.HeadLayerAlphaHolder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @WrapOperation(
            method = "renderQuadList",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFII)V"
            )
    )
    private static void renderQuadList(
            VertexConsumer instance,
            PoseStack.Pose pose,
            BakedQuad quad,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight,
            int packedOverlay,
            Operation<Void> original
    ) {
        if (HeadLayerAlphaHolder.alpha != null) {
            original.call(
                    instance,
                    pose,
                    quad,
                    red,
                    green,
                    blue,
                    0x89 / 255.0f,
                    packedLight,
                    packedOverlay);
            return;
        }

        original.call(instance, pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
    }

}
