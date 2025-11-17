package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.ClearableLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GridLayout.class)
public class GridLayoutMixin implements ClearableLayout {
    @Shadow
    @Final
    private List<LayoutElement> children;

    @Override
    public void skyocean$clear() {
        children.clear();
    }
}
