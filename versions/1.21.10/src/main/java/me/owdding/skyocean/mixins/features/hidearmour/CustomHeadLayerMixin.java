package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.accessors.hidearmour.PlayerRenderStateAccessor;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import me.owdding.skyocean.helpers.HeadLayerAlphaHolder;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomHeadLayer.class)
public class CustomHeadLayerMixin {

    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;submitSkull(Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
    )
    public <S extends LivingEntityRenderState> void render(
        Direction direction,
        float v,
        float v2,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int i,
        SkullModelBase skullModelBase,
        RenderType renderType,
        int i2,
        ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
        Operation<Void> original,
        @Local(argsOnly = true) S renderState
    ) {
        if (renderState instanceof PlayerRenderStateAccessor accessor && !accessor.skyocean$isNpc()) {
            if (accessor.skyocean$isSelf()) {
                HeadLayerAlphaHolder.alpha = MiscConfig.INSTANCE.getTransparentArmorSelf();
            } else {
                HeadLayerAlphaHolder.alpha = MiscConfig.INSTANCE.getTransparentArmorOthers();
            }
        }
        original.call(direction, v, v2, poseStack, submitNodeCollector, i, skullModelBase, renderType, i2, crumblingOverlay);
        HeadLayerAlphaHolder.alpha = null;
    }

    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
        )
    )
    public <S extends LivingEntityRenderState> void render(
        ItemStackRenderState instance,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int packedLight,
        int packedOverlay,
        int outlineColor,
        Operation<Void> original,
        @Local(argsOnly = true) S renderState
    ) {
        if (renderState instanceof PlayerRenderStateAccessor accessor && !accessor.skyocean$isNpc()) {
            if (accessor.skyocean$isSelf()) {
                HeadLayerAlphaHolder.alpha = MiscConfig.INSTANCE.getTransparentArmorSelf();
            } else {
                HeadLayerAlphaHolder.alpha = MiscConfig.INSTANCE.getTransparentArmorOthers();
            }
        }
        original.call(instance, poseStack, submitNodeCollector, packedLight, packedOverlay, outlineColor);
        HeadLayerAlphaHolder.alpha = null;
    }
}
