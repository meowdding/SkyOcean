package me.owdding.skyocean.accessors;

import java.util.UUID;

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

    UUID ocean$getUUID();

    void ocean$setUUID(UUID uuid);

}
