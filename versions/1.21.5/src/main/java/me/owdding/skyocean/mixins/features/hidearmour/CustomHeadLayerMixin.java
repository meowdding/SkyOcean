package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import me.owdding.skyocean.helpers.HeadLayerAlphaHolder;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomHeadLayer.class)
public class CustomHeadLayerMixin {

    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;renderSkull(Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;)V")
    )
    public <S extends LivingEntityRenderState> void render(
        Direction direction,
        float yRot,
        float mouthAnimation,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        SkullModelBase model,
        RenderType renderType,
        Operation<Void> original,
        @Local(argsOnly = true) S renderState
    ) {
        if (renderState instanceof AvatarRenderStateAccessor accessor && !accessor.skyocean$isNpc()) {
            if (accessor.skyocean$isSelf()) {
                HeadLayerAlphaHolder.alpha = MiscConfig.INSTANCE.getTransparentArmorSelf();
            } else {
                HeadLayerAlphaHolder.alpha = MiscConfig.INSTANCE.getTransparentArmorOthers();
            }
        }
        original.call(direction, yRot, mouthAnimation, poseStack, bufferSource, packedLight, model, renderType);
        HeadLayerAlphaHolder.alpha = null;
    }

    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
        )
    )
    public <S extends LivingEntityRenderState> void render(
        ItemStackRenderState instance,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay,
        Operation<Void> original,
        @Local(argsOnly = true) S renderState
    ) {
        if (renderState instanceof AvatarRenderStateAccessor accessor && !accessor.skyocean$isNpc()) {
            if (accessor.skyocean$isSelf()) {
                HeadLayerAlphaHolder.alpha = MiscConfig.INSTANCE.getTransparentArmorSelf();
            } else {
                HeadLayerAlphaHolder.alpha = MiscConfig.INSTANCE.getTransparentArmorOthers();
            }
        }
        original.call(instance, poseStack, bufferSource, packedLight, packedOverlay);
        HeadLayerAlphaHolder.alpha = null;
    }
}
