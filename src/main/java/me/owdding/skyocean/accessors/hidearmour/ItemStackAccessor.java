package me.owdding.skyocean.accessors.hidearmour;

import net.minecraft.world.item.ItemStack;

public interface ItemStackAccessor {

    static Integer getAlpha(ItemStack itemStack) {
        if (((Object) itemStack) instanceof ItemStackAccessor accessor) {
            return accessor.skyocean$getAlpha();
        }
        return null;
    }

    static void setAlpha(ItemStack itemStack, Integer alpha) {
        if (((Object) itemStack) instanceof ItemStackAccessor accessor) {
            accessor.skyocean$setAlpha(alpha);
        }
    }

    void skyocean$setAlpha(Integer alpha);

    Integer skyocean$getAlpha();

}
