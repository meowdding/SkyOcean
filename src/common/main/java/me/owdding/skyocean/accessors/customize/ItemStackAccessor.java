package me.owdding.skyocean.accessors.customize;

import me.owdding.skyocean.features.item.custom.data.ItemKey;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface ItemStackAccessor {

    @Nullable
    static ItemKey getItemKey(ItemStack stack) {
        if (((Object) stack) instanceof ItemStackAccessor accessor) {
            return accessor.skyocean$getItemKey();
        }
        return null;
    }

    @Nullable
    ItemKey skyocean$getItemKey();

}
