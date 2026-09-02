package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import me.owdding.skyocean.features.garden.cropfever.CropFeverEffects;

@Mixin(Minecraft.class)
public abstract class MinecraftPerspectiveMixin {

    @WrapOperation(
        method = "handleKeybinds",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V")
    )
    private void redirectCheckEntityPostEffect(GameRenderer instance, Entity ignored, Operation<Void> original) {
        if (CropFeverEffects.isFeverActive()) {
            return;
        }
        original.call(instance, ignored);
    }
}
