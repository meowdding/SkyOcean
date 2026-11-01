package me.owdding.skyocean.mixins;

import me.owdding.skyocean.helpers.EntityAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class EntityMixin implements EntityAccessor {
    @Unique private float ocean$nameTagScale = 1f;

    @Override
    public float ocean$getNameTagScale() {
        return this.ocean$nameTagScale;
    }

    @Override
    public void ocean$setNameTagScale(float scale) {
        this.ocean$nameTagScale = scale;
    }
}
