package me.owdding.skyocean.hooks;

import com.mojang.blaze3d.platform.Transparency;

public interface FluidModelTransparencyOverride {

    default Transparency skyocean$getTransparency() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default void skyocean$setTransparency(Transparency value) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}
