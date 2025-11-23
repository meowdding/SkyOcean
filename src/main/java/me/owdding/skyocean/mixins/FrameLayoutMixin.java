package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.ClearableLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(FrameLayout.class)
public class FrameLayoutMixin implements ClearableLayout {
    @Shadow
    @Final
    private List<FrameLayout.ChildContainer> children;

    @Override
    public void skyocean$clear() {
        this.children.clear();
    }
}
