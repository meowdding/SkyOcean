package me.owdding.skyocean.accessors;

import java.util.UUID;

import kotlin.time.Instant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface AvatarRenderStateAccessor {

    static void setUUID(Object renderState, UUID uuid) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            accessor.ocean$setUUID(uuid);
        }
    }

    static UUID getUUID(Object renderState) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            return accessor.ocean$getUUID();
        }
        return null;
    }

    static boolean isSelf(Object renderState) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            return accessor.skyocean$isSelf();
        }
        return false;
    }

    static void setSelf(Object renderState, boolean isSelf) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            accessor.skyocean$setSelf(isSelf);
        }
    }

    static boolean isNpc(Object renderState) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            return accessor.skyocean$isNpc();
        }
        return false;
    }

    static void setNpc(Object renderState, boolean isNpc) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            accessor.skyocean$setNpc(isNpc);
        }
    }

    @Nullable
    static LivingEntityRenderState getAnimalState(Object renderState) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            return accessor.skyocean$getAnimalState();
        }
        return null;
    }

    static void setAnimalState(Object renderState, LivingEntityRenderState state) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            accessor.skyocean$setAnimalState(state);
        }
    }

    static ItemStack getHeldItemStack(Object renderState) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            return accessor.ocean$getHeldItemStack();
        }
        return ItemStack.EMPTY;
    }

    static void setHeldItemStack(Object renderState, ItemStack stack) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            accessor.ocean$setHeldItemStack(stack);
        }
    }

    static Instant getLastMoveTime(Object renderState) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            return accessor.ocean$getLastMoveTime();
        }
        return null;
    }

    static void setLastMoveTime(Object renderState, Instant time) {
        if (renderState instanceof AvatarRenderStateAccessor accessor) {
            accessor.ocean$setLastMoveTime(time);
        }
    }

    void ocean$setUUID(UUID uuid);

    UUID ocean$getUUID();

    void skyocean$setSelf(boolean isSelf);

    boolean skyocean$isSelf();

    void skyocean$setNpc(boolean isNpc);

    boolean skyocean$isNpc();

    LivingEntityRenderState skyocean$getAnimalState();

    void skyocean$setAnimalState(LivingEntityRenderState state);

    ItemStack ocean$getHeldItemStack();

    void ocean$setHeldItemStack(ItemStack stack);

    Instant ocean$getLastMoveTime();

    void ocean$setLastMoveTime(Instant time);

}
