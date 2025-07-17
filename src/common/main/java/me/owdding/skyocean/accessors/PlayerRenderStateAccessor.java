package me.owdding.skyocean.accessors;

import net.minecraft.client.renderer.entity.state.PlayerRenderState;

import java.util.UUID;

public interface PlayerRenderStateAccessor {

    static void setUUID(PlayerRenderState renderState, UUID uuid) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            accessor.ocean$setUUID(uuid);
        }
    }

    static UUID getUUID(PlayerRenderState renderState) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            return accessor.ocean$getUUID();
        }
        return null;
    }

    UUID ocean$getUUID();

    void ocean$setUUID(UUID uuid);

}
