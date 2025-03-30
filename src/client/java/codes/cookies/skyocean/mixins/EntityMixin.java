package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.helpers.EntityAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements EntityAccessor {

    @Unique
    private boolean ocean$glowing = false;
    @Unique
    private int ocean$glowingColor = 0;

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void getTeamColor(CallbackInfoReturnable<Integer> cir) {
        if (ocean$glowing) {
            cir.setReturnValue(ocean$glowingColor);
        }
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    public void isGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (ocean$glowing) {
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
}
