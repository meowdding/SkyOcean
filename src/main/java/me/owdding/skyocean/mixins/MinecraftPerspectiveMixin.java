package me.owdding.skyocean.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import me.owdding.skyocean.features.garden.CropFeverEffects;

@Mixin(Minecraft.class)
public abstract class MinecraftPerspectiveMixin {

    @Redirect(
        method = "handleKeybinds",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V")
    )
    private void redirectCheckEntityPostEffect(GameRenderer instance, net.minecraft.world.entity.Entity entity) {
        if (CropFeverEffects.isFeverActive()) {
            return;
        }
        instance.checkEntityPostEffect(entity);
    }
}
