package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.ClearableLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LinearLayout.class)
public class LinearLayoutMixin implements ClearableLayout {
    @Shadow
    @Final
    private GridLayout wrapped;

    @Override
    public void skyocean$clear() {
        if (wrapped instanceof ClearableLayout clearableLayout) {
            clearableLayout.skyocean$clear();
        }
    }
}
