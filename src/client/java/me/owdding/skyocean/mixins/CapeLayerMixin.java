package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.owdding.skyocean.accessors.PlayerRenderStateAccessor;
import me.owdding.skyocean.utils.ContributorHandler;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeLayer.class)
public class CapeLayerMixin {

    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/PlayerSkin;capeTexture()Lnet/minecraft/resources/ResourceLocation;"
        )
    )
    private ResourceLocation render(PlayerSkin instance, Operation<ResourceLocation> original, @Local(argsOnly = true) PlayerRenderState playerRenderState) {
        var uuid = PlayerRenderStateAccessor.getUUID(playerRenderState);
        if (uuid == null) {
            return original.call(instance);
        }

        if (ContributorHandler.INSTANCE.getContributors().containsKey(uuid)) {
            return ContributorHandler.INSTANCE.getContributors().get(uuid).getCape();
        }

        return original.call(instance);
    }

}
