package me.owdding.skyocean.mixins;

import me.owdding.skyocean.helpers.EntityRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateAccessor {

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
