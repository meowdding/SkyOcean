package me.owdding.skyocean.mixins.features.playeranimals;

import kotlin.time.Instant;
import me.owdding.skyocean.accessors.WalkAnimationStateAccessor;
import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent;
import tech.thatgravyboat.skyblockapi.utils.extentions.InstantExtensionsKt;

@Mixin(WalkAnimationState.class)
public class WalkAnimationStateMixin implements WalkAnimationStateAccessor {

    @Unique
    private Integer ocean$startMoveTime;

    @Inject(method = "update", at = @At("TAIL"))
    public void update(CallbackInfo ci) {
        if (ocean$startMoveTime == null) {
            ocean$startMoveTime = TickEvent.INSTANCE.getTicks();
        }
    }

    @Inject(method = "stop", at = @At("TAIL"))
    public void stop(CallbackInfo ci) {
        this.ocean$startMoveTime = null;
    }

    @Override
    public Integer ocean$getStartMoveTime() {
        return this.ocean$startMoveTime;
    }
}
