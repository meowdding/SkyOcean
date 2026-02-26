package me.owdding.skyocean.mixins;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import me.owdding.skyocean.accessors.ItemStackAttachmentAccessor;
import me.owdding.skyocean.utils.items.ItemAttachmentKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackAttachmentAccessor {
    @Unique
    private Map<ItemAttachmentKey<?>, Object> itemAttachments = null;

    @Override
    public <T> T skyocean$getData(@NotNull ItemAttachmentKey<T> key) {
        //noinspection unchecked
        return itemAttachments == null ? null : (T) itemAttachments.get(key);
    }

    @Override
    public <T> @Nullable T skyocean$remove(@NotNull ItemAttachmentKey<T> key) {
        //noinspection unchecked
        return itemAttachments == null ? null : (T) itemAttachments.remove(key);
    }

    @Override
    public <T> void skyocean$setData(@NotNull ItemAttachmentKey<T> key, @Nullable T value) {
        ensureMapIsPresent();
        itemAttachments.put(key, value);
    }

    @Override
    public <T> boolean skyocean$hasKey(@NotNull ItemAttachmentKey<T> key) {
        return this.itemAttachments != null && this.itemAttachments.containsKey(key);
    }

    @Override
    public @NotNull Map<ItemAttachmentKey<?>, Object> skyocean$getAll() {
        return this.itemAttachments != null ? this.itemAttachments : Collections.emptyMap();
    }

    @Override
    public void skyocean$putAll(@NotNull Map<ItemAttachmentKey<?>, Object> map) {
        if (map.isEmpty()) return;
        ensureMapIsPresent();
        this.itemAttachments.putAll(map);
    }

    @Unique
    private void ensureMapIsPresent() {
        if (itemAttachments != null) return;
        itemAttachments = new IdentityHashMap<>();
    }
}
