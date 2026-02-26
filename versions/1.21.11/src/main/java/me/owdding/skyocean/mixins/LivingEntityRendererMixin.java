package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import kotlin.time.Instant;
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor;
import me.owdding.skyocean.features.misc.fun.animal.PlayerAnimals;
import me.owdding.skyocean.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer<LivingEntity, LivingEntityRenderState> {

    @Shadow
    public abstract void extractRenderState(LivingEntity par1, LivingEntityRenderState par2, float par3);

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @WrapMethod(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    protected <S extends LivingEntityRenderState> void submit(
        S renderState,
        PoseStack poseStack,
        SubmitNodeCollector nodeCollector,
        CameraRenderState cameraRenderState,
        Operation<Void> original
    ) {
        original.call(renderState, poseStack, nodeCollector, cameraRenderState);
    }

    @Mixin(AvatarRenderer.class)
    private static abstract class AvatarRendererMixin extends LivingEntityRendererMixin {

        protected AvatarRendererMixin(EntityRendererProvider.Context context) {
            super(context);
        }

        @WrapMethod(method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;")
        public Vec3 getRenderOffset(AvatarRenderState avatarRenderState, Operation<Vec3> original) {
            if (PlayerAnimals.shouldPlayerBeAnimal(avatarRenderState)) {
                return super.getRenderOffset(avatarRenderState);
            }
            return original.call(avatarRenderState);
        }

        @WrapMethod(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V")
        public <AvatarlikeEntity extends Avatar> void extractRenderState(
            AvatarlikeEntity avatar,
            AvatarRenderState state,
            float partialTick,
            Operation<Void> original
        ) {
            original.call(avatar, state, partialTick);
            AvatarRenderStateAccessor.setUUID(state, avatar.getUUID());
            AvatarRenderStateAccessor.setSelf(state, avatar instanceof LocalPlayer);
            AvatarRenderStateAccessor.setNpc(state, avatar.getUUID().version() != 4);
            AvatarRenderStateAccessor.setAnimalState(state, null);
            if (!PlayerAnimals.shouldPlayerBeAnimal(state)) {
                return;
            }
            AvatarRenderStateAccessor.setHeldItemStack(state, avatar.getMainHandItem());

            if (avatar instanceof AbstractClientPlayer) {
                Instant lastMoveTime = PlayerUtils.INSTANCE.getLastMoveTime(avatar.getUUID());
                AvatarRenderStateAccessor.setLastMoveTime(state, lastMoveTime);
            }

            var type = PlayerAnimals.getEntityType();
            var renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(type);
            var renderState = renderer.createRenderState();

            if (!(renderState instanceof LivingEntityRenderState livingState)) {
                return;
            }

            PlayerAnimals.INSTANCE.getRenderer().extractRenderState(avatar, livingState, partialTick);
            livingState.shadowRadius = state.shadowRadius;
            livingState.shadowPieces.clear();
            livingState.shadowPieces.addAll(state.shadowPieces);

            livingState.entityType = type;
            PlayerAnimals.apply(avatar, state, livingState, partialTick);

            AvatarRenderStateAccessor.setAnimalState(state, livingState);
        }

        @Override
        protected <S extends LivingEntityRenderState> void submit(
            S avatarState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            CameraRenderState cameraRenderState,
            Operation<Void> original
        ) {
            var otherState = AvatarRenderStateAccessor.getAnimalState(avatarState);
            if (otherState != null) {
                Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(otherState).submit(otherState, poseStack, nodeCollector, cameraRenderState);
            } else {
                original.call(avatarState, poseStack, nodeCollector, cameraRenderState);
            }
        }
    }

}
