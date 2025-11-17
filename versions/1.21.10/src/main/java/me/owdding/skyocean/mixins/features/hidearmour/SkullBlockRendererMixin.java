package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.helpers.HeadLayerAlphaHolder;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {

    @WrapOperation(
        method = "submitSkull",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
        )
    )
    private static <S> void renderSkull(
        SubmitNodeCollector instance,
        Model<S> model,
        S o,
        PoseStack poseStack,
        RenderType renderType,
        int packedLight,
        int packedOverlay,
        int outlineColor,
        ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
        Operation<Void> original
    ) {
        if (HeadLayerAlphaHolder.alpha != null) {
            instance.submitModel(
                model,
                o,
                poseStack,
                renderType,
                packedLight,
                packedOverlay,
                (HeadLayerAlphaHolder.alpha << 24) | 0xFFFFFF,
                null,
                outlineColor,
                crumblingOverlay);
            return;
        }

        original.call(instance, model, o, poseStack, renderType, packedLight, packedOverlay, outlineColor, crumblingOverlay);
    }

}
