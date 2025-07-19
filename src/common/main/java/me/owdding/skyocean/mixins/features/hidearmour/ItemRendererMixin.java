package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.owdding.skyocean.helpers.HeadLayerAlphaHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @WrapMethod(method = "renderItem")
    private static void renderItem(
        ItemDisplayContext displayContext,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay,
        int[] tintLayers,
        List<BakedQuad> quads,
        RenderType renderType,
        ItemStackRenderState.FoilType foilType,
        Operation<Void> original
    ) {
        if (HeadLayerAlphaHolder.alpha != null) {
            renderType = Sheets.translucentItemSheet();
        }
        original.call(displayContext, poseStack, bufferSource, packedLight, packedOverlay, tintLayers, quads, renderType, foilType);
    }

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
                ARGB.blueFloat(HeadLayerAlphaHolder.alpha),
                packedLight,
                packedOverlay);
            return;
        }

        original.call(instance, pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
    }

}
