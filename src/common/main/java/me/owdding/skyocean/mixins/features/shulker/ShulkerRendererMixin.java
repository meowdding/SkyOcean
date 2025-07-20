package me.owdding.skyocean.mixins.features.shulker;

import me.owdding.skyocean.config.features.foraging.GalateaConfig;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland;

@Mixin(ShulkerRenderer.class)
public class ShulkerRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/Shulker;Lnet/minecraft/client/renderer/entity/state/ShulkerRenderState;F)V", at = @At("TAIL"))
    public void setShulkerRenderState(Shulker shulker, ShulkerRenderState shulkerRenderState, float f, CallbackInfo ci) {
        if (!SkyBlockIsland.GALATEA.inIsland()) {
            return;
        }
        if (GalateaConfig.INSTANCE.getShulkerOverwrite().getDye() == shulker.getColor()) {
            return;
        }
        shulkerRenderState.color = GalateaConfig.INSTANCE.getShulkerOverwrite().getDye();
    }

}
