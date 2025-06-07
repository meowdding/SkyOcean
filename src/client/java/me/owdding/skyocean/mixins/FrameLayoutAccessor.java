package me.owdding.skyocean.mixins;

import net.minecraft.client.gui.layouts.FrameLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(FrameLayout.class)
public interface FrameLayoutAccessor {

    @Accessor("children")
    List<?> children();

}
