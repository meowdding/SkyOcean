package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.helpers.HeadLayerAlphaHolder;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.rendertype.RenderType;
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
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;III)V"
        )
    )
    private static <S> void renderSkull(
        SubmitNodeCollector instance,
        final Model<? super S> model,
        final S state,
        final PoseStack poseStack,
        final RenderType renderType,
        final int lightCoords,
        final int overlayCoords,
        final int outlineColor,
        //? < 26.3
        //ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
        Operation<Void> original
    ) {
        if (HeadLayerAlphaHolder.alpha != null) {
            instance.submitModel(
                model,
                state,
                poseStack,
                renderType,
                lightCoords,
                overlayCoords,
                (HeadLayerAlphaHolder.alpha << 24) | 0xFFFFFF,
                null,
                outlineColor
                //? < 26.3
                //,crumblingOverlay
            );
            return;
        }

        original.call(instance, model, state, poseStack, renderType, lightCoords, overlayCoords, outlineColor);
    }

}
