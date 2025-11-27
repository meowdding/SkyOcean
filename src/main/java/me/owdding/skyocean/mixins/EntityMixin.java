package me.owdding.skyocean.mixins;

import me.owdding.skyocean.helpers.EntityAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements EntityAccessor {

    @Unique private boolean ocean$glowing = false;
    @Unique private int ocean$glowingColor = 0;
    @Unique
    private long ocean$glowTime = -1;
    @Unique private float ocean$nameTagScale = 1f;

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void getTeamColor(CallbackInfoReturnable<Integer> cir) {
        if (hasCustomGlow()) {
            cir.setReturnValue(ocean$glowingColor);
        }
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    public void isGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (hasCustomGlow()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void ocean$setGlowing(boolean glowing) {
        this.ocean$glowing = glowing;
    }

    @Override
    public void ocean$setGlowingColor(int color) {
        this.ocean$glowingColor = color;
    }

    @Override
    public float ocean$getNameTagScale() {
        return this.ocean$nameTagScale;
    }

    @Override
    public void ocean$setNameTagScale(float scale) {
        this.ocean$nameTagScale = scale;
    }

    @Override
    public void ocean$glowTime(long time) {
        this.ocean$glowTime = System.currentTimeMillis() + time;
        this.ocean$glowing = false;
    }

    @Unique
    private boolean hasCustomGlow() {
        if (this.ocean$glowTime > System.currentTimeMillis()) {
            return true;
        }

        this.ocean$glowTime = -1;
        return this.ocean$glowing;
    }
}
