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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
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


    @WrapOperation(
        method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
        at = @At(
            value = "INVOKE",
            //~ if >= 26.2 'ZIDLnet' -> 'ZILnet'
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
        )
    )
    public void scaleNameTag(
        SubmitNodeCollector instance,
        PoseStack poseStack,
        @Nullable Vec3 nameTagAttachment,
        final int offset,
        Component name,
        boolean seeThrough,
        int lightCoords,
        //? 26.1
         //double distanceToCamera,
        CameraRenderState cameraRenderState,
        Operation<Void> original,
        @Local(argsOnly = true) EntityRenderState state
    ) {
        float scale = 1f;
        if (state instanceof EntityRenderStateAccessor stateAccessor) {
            scale = stateAccessor.ocean$getNameTagScale();
        }
        if (scale != 1f) {
            poseStack.pushPose();
            poseStack.translate(0, -0.7 * (scale / 5), 0);
            poseStack.scale(scale, scale, scale);
        }
        original.call(instance,
            poseStack,
            nameTagAttachment,
            offset,
            name,
            seeThrough,
            lightCoords,
            //? 26.1
            //distanceToCamera,
            cameraRenderState
        );
        if (scale != 1f) {
            poseStack.popPose();
        }
    }
}
