package me.owdding.skyocean.mixins.features.hidearmour;

import me.owdding.skyocean.accessors.hidearmour.PlayerRenderStateAccessor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("HEAD")
    )
    private void extractRenderState(Avatar avatar, AvatarRenderState renderState, float $$2, CallbackInfo ci) {
        PlayerRenderStateAccessor.isSelf(renderState, avatar instanceof LocalPlayer);
        PlayerRenderStateAccessor.setNpc(renderState, avatar.getUUID().version() != 4);
    }

}
