package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.owdding.skyocean.events.RenderTranslucentFeatures;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;
    @Unique
    private final ThreadLocal<DeltaTracker> deltaTracker = new ThreadLocal<>();


    @Inject(method = "addMainPass", at = @At("HEAD"))
    public void saveDeltaTracker(CallbackInfo ci, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        this.deltaTracker.set(deltaTracker);
    }


    @Inject(
        method = "lambda$addMainPass$0", at = @At(
        value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderTranslucentFeatures()V"
    )
    )
    public void invoke(
        CallbackInfo ci,
        @Local PoseStack poseStack,
        @Local(ordinal = 0) MultiBufferSource.BufferSource source,
        @Local(argsOnly = true) LevelRenderState levelRenderState) {
        new RenderTranslucentFeatures(new RenderWorldEvent.AfterTranslucent(
            poseStack,
            source,
            submitNodeStorage,
            levelRenderState.cameraRenderState.pos,
            levelRenderState.cameraRenderState.orientation,
            deltaTracker.get().getGameTimeDeltaPartialTick(false)
        )).post(SkyBlockAPI.getEventBus());

    }

}
