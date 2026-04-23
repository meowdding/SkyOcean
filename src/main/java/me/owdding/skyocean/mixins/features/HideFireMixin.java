package me.owdding.skyocean.mixins.features;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class HideFireMixin {

    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitFlame(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lorg/joml/Quaternionf;)V"))
    public void renderFlame(
        SubmitNodeCollector instance,
        PoseStack poseStack,
        EntityRenderState entityRenderState,
        Quaternionf quaternionf,
        Operation<Void> original
    ) {
        if (!MiscConfig.INSTANCE.getHideEntityFire()) {
            original.call(instance, poseStack, entityRenderState, quaternionf);
        }
    }

}
