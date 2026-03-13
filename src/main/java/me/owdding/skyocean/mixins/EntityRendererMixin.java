package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.helpers.EntityAccessor;
import me.owdding.skyocean.helpers.EntityRenderStateAccessor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void extractRenderState(T entity, S reusedState, float partialTick, CallbackInfo ci) {
        if (reusedState instanceof EntityRenderStateAccessor stateAccessor && entity instanceof EntityAccessor entityAccessor) {
            stateAccessor.ocean$setNameTagScale(entityAccessor.ocean$getNameTagScale());
        }
    }

    @WrapOperation(method = "submitNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V"))
    public void scaleNameTag(
        SubmitNodeCollector instance,
        PoseStack poseStack,
        Vec3 vec3,
        int i,
        Component component,
        boolean b,
        int i2,
        double v,
        CameraRenderState cameraRenderState,
        Operation<Void> original,
        @Local(argsOnly = true) EntityRenderState state
    ) {
        if (state instanceof EntityRenderStateAccessor stateAccessor) {
            float scale = stateAccessor.ocean$getNameTagScale();
            poseStack.pushPose();
            poseStack.translate(0, -0.7 * (scale / 5), 0);
            poseStack.scale(1 * scale, 1 * scale, 1 * scale);
            original.call(instance, poseStack, vec3, i, component, b, i2, v, cameraRenderState);
            poseStack.popPose();
        } else {
            original.call(instance, poseStack, vec3, i, component, b, i2, v, cameraRenderState);
        }
    }
}
