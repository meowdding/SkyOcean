package me.owdding.skyocean.accessors;

import java.util.Map;
import me.owdding.skyocean.utils.items.ItemAttachmentKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemStackAttachmentAccessor {

    <T> @Nullable T skyocean$getData(@NotNull ItemAttachmentKey<T> key);
    <T> @Nullable T skyocean$remove(@NotNull ItemAttachmentKey<T> key);
    <T> void skyocean$setData(@NotNull ItemAttachmentKey<T> key, @Nullable T value);
    <T> boolean skyocean$hasKey(@NotNull ItemAttachmentKey<T> key);
    @NotNull Map<ItemAttachmentKey<?>, Object> skyocean$getAll();
    void skyocean$putAll(@NotNull Map<ItemAttachmentKey<?>, Object> map);

}
