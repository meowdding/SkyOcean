package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.helpers.EntityAccessor;
import me.owdding.skyocean.helpers.EntityRenderStateAccessor;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
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

    @WrapOperation(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    public void scaleNameTag(PoseStack instance, float x, float y, float z, Operation<Void> original, @Local(argsOnly = true) EntityRenderState state) {
        if (state instanceof EntityRenderStateAccessor stateAccessor) {
            float scale = stateAccessor.ocean$getNameTagScale();
            instance.translate(0, 0.5 * scale - 0.5, 0);
            original.call(instance, x * scale, y * scale, z * scale);
        } else {
            original.call(instance, x, y, z);
        }
    }
}
