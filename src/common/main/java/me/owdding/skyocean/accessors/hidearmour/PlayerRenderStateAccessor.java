package me.owdding.skyocean.accessors.hidearmour;

import net.minecraft.client.renderer.entity.state.PlayerRenderState;

public interface PlayerRenderStateAccessor {
    static boolean isSelf(PlayerRenderState renderState) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            return accessor.skyocean$isSelf();
        }
        return false;
    }

    static void isSelf(PlayerRenderState renderState, boolean isSelf) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            accessor.skyocean$setSelf(isSelf);
        }
    }

    static boolean isNpc(PlayerRenderState renderState) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            return accessor.skyocean$isNpc();
        }
        return false;
    }

    static void setNpc(PlayerRenderState renderState, boolean isNpc) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            accessor.skyocean$setNpc(isNpc);
        }
    }

    void skyocean$setSelf(boolean isSelf);

    boolean skyocean$isSelf();

    void skyocean$setNpc(boolean isNpc);

    boolean skyocean$isNpc();

}
