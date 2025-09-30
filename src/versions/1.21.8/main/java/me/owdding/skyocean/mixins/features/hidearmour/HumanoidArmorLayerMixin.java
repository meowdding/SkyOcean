package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.accessors.hidearmour.ItemStackAccessor;
import me.owdding.skyocean.accessors.hidearmour.PlayerRenderStateAccessor;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V")
    )
    public <S extends HumanoidRenderState, A extends HumanoidModel<S>> void render(
        @SuppressWarnings("rawtypes") HumanoidArmorLayer instance,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        ItemStack armorItem,
        EquipmentSlot slot,
        int packedLight,
        A model,
        Operation<Void> original,
        @Local(argsOnly = true) S renderState
    ) {
        if (renderState instanceof PlayerRenderStateAccessor accessor && !accessor.skyocean$isNpc()) {
            if (accessor.skyocean$isSelf()) {
                ItemStackAccessor.setAlpha(armorItem, MiscConfig.INSTANCE.getTransparentArmorSelf());
            } else {
                ItemStackAccessor.setAlpha(armorItem, MiscConfig.INSTANCE.getTransparentArmorOthers());
            }
        }


        //noinspection MixinExtrasOperationParameters
        original.call(instance, poseStack, bufferSource, armorItem, slot, packedLight, model);
    }

}
