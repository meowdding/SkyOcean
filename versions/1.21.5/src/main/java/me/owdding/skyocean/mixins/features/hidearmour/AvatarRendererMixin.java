package me.owdding.skyocean.mixins.features.hidearmour;

import me.owdding.skyocean.accessors.hidearmour.PlayerRenderStateAccessor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class AvatarRendererMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V",
        at = @At("HEAD")
    )
    private void extractRenderState(AbstractClientPlayer player, PlayerRenderState renderState, float ticks, CallbackInfo ci) {
        PlayerRenderStateAccessor.isSelf(renderState, player instanceof LocalPlayer);
        PlayerRenderStateAccessor.setNpc(renderState, player.getUUID().version() != 4);
    }

}
