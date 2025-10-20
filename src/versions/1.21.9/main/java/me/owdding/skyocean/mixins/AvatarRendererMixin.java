package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.AvatarRenderStateAccessor;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    public void extractRenderState(Avatar abstractClientPlayer, AvatarRenderState playerRenderState, float f, CallbackInfo ci) {
        AvatarRenderStateAccessor.setUUID(playerRenderState, abstractClientPlayer.getUUID());
    }

}
