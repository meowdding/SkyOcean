package me.owdding.skyocean.mixins.features.fishing;

//? if >= 26.1 {
import me.owdding.skyocean.hooks.FluidModelTransparencyOverride;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidModel.class)
public class FluidModelMixin implements FluidModelTransparencyOverride {

    @Unique
    private @Nullable Transparency skyocean$transparency = null;

    @Override
    public @Nullable Transparency skyocean$getTransparency() {
        return skyocean$transparency;
    }

    @Override
    public void skyocean$setTransparency(@Nullable Transparency value) {
        skyocean$transparency = value;
    }

    @ModifyReturnValue(method = "layer", at = @At("RETURN"))
    private ChunkSectionLayer skyocean$overrideTransparency(ChunkSectionLayer original) {
        if (skyocean$transparency != null) return ChunkSectionLayer.byTransparency(skyocean$transparency);
        return original;
    }
}
//? }
