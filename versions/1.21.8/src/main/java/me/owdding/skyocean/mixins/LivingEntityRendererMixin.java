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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer<Entity, EntityRenderState> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @WrapMethod(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    protected  <S extends LivingEntityRenderState> void render(
        S livingEntityRenderState,
        PoseStack poseStack,
        MultiBufferSource multiBufferSource,
        int i,
        Operation<Void> original
    ) {
        original.call(livingEntityRenderState, poseStack, multiBufferSource, i);
    }


    @Mixin(PlayerRenderer.class)
    private static abstract class PlayerRendererMixin extends LivingEntityRendererMixin {

        protected PlayerRendererMixin(EntityRendererProvider.Context context) {
            super(context);
        }

        @WrapMethod(method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)Lnet/minecraft/world/phys/Vec3;")
        public Vec3 getRenderOffset(PlayerRenderState playerState, Operation<Vec3> original) {
            if (PlayerAnimals.shouldPlayerBeAnimal(playerState)) {
                return super.getRenderOffset(playerState);
            }
            return original.call(playerState);
        }


        @WrapMethod(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V")
        public void extractRenderState(AbstractClientPlayer entity, PlayerRenderState state, float partialTick, Operation<Void> original) {
            original.call(entity, state, partialTick);
            AvatarRenderStateAccessor.setUUID(state, entity.getUUID());
            AvatarRenderStateAccessor.setSelf(state, entity instanceof LocalPlayer);
            AvatarRenderStateAccessor.setNpc(state, entity.getUUID().version() != 4);
            AvatarRenderStateAccessor.setAnimalState(state, null);
            if (!PlayerAnimals.shouldPlayerBeAnimal(state)) {
                return;
            }
            AvatarRenderStateAccessor.setHeldItemStack(state, entity.getMainHandItem());

            if (entity instanceof AbstractClientPlayer) {
                Instant lastMoveTime = PlayerUtils.INSTANCE.getLastMoveTime(entity.getUUID());
                AvatarRenderStateAccessor.setLastMoveTime(state, lastMoveTime);
            }
            
            var type = PlayerAnimals.getEntityType();
            var renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(type);
            var renderState = renderer.createRenderState();

            if (!(renderState instanceof LivingEntityRenderState livingState)) {
                return;
            }

            PlayerAnimals.INSTANCE.getRenderer().extractRenderState(entity, livingState, partialTick);

            livingState.entityType = type;
            PlayerAnimals.apply(entity, state, livingState, partialTick);

            AvatarRenderStateAccessor.setAnimalState(state, livingState);
        }

        @Override
        protected <S extends LivingEntityRenderState> void render(
            S playerState,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i,
            Operation<Void> original
        ) {
            var otherState = AvatarRenderStateAccessor.getAnimalState(playerState);
            if (otherState != null) {
                Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(otherState).render(otherState, poseStack, multiBufferSource, i);
            } else {
                original.call(playerState, poseStack, multiBufferSource, i);
            }
        }
    }

}
