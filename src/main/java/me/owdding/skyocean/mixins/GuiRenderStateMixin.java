//? if > 1.21.5 {
package me.owdding.skyocean.mixins;

import me.owdding.skyocean.utils.rendering.PostEffectApplicator;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public abstract class GuiRenderStateMixin implements PostEffectApplicator {

    @Shadow
    private int firstStratumAfterBlur;

    @Shadow
    public abstract void blurBeforeThisStratum();

    @Unique
    private ResourceLocation skyocean$currentEffect = null;

    @Inject(method = "reset", at = @At("TAIL"))
    private void skyocean$resetPostEffect(CallbackInfo ci) {
        this.skyocean$currentEffect = null;
    }

    @Override
    public void skyocean$applyPostEffect(@NotNull ResourceLocation id) {
        this.firstStratumAfterBlur = Integer.MAX_VALUE;
        this.blurBeforeThisStratum();
        this.skyocean$currentEffect = id;
    }

    @Override
    public @Nullable ResourceLocation skyocean$getPostEffect() {
        return this.skyocean$currentEffect;
    }
}
