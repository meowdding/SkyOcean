package me.owdding.skyocean.mixins.features.hidearmour;

import me.owdding.skyocean.accessors.hidearmour.ItemStackAccessor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackAccessor {

    @Unique
    private Integer skyocean$alpha = null;

    @Override
    public void skyocean$setAlpha(Integer alpha) {
        this.skyocean$alpha = alpha;
    }

    @Override
    public Integer skyocean$getAlpha() {
        return this.skyocean$alpha;
    }
}
