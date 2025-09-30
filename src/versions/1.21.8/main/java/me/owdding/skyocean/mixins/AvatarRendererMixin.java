package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.AvatarRenderStateAccessor;
import me.owdding.skyocean.utils.ContributorHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class AvatarRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("TAIL"))
    public void extractRenderState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f, CallbackInfo ci) {
        AvatarRenderStateAccessor.setUUID(playerRenderState, abstractClientPlayer.getUUID());

        var contributor = ContributorHandler.INSTANCE.getContributors().get(abstractClientPlayer.getUUID());
        if (contributor == null) {
            return;
        }

        var skin = playerRenderState.skin;
        playerRenderState.skin = new PlayerSkin(
            skin.texture(),
            skin.textureUrl(),
            contributor.getCape().id(),
            skin.elytraTexture(),
            skin.model(),
            skin.secure()
        );
    }

}
