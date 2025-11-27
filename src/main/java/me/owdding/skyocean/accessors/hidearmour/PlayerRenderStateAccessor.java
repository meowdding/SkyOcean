package me.owdding.skyocean.accessors.hidearmour;

public interface PlayerRenderStateAccessor {
    static boolean isSelf(Object renderState) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            return accessor.skyocean$isSelf();
        }
        return false;
    }

    static void isSelf(Object renderState, boolean isSelf) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            accessor.skyocean$setSelf(isSelf);
        }
    }

    static boolean isNpc(Object renderState) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            return accessor.skyocean$isNpc();
        }
        return false;
    }

    static void setNpc(Object renderState, boolean isNpc) {
        if (renderState instanceof PlayerRenderStateAccessor accessor) {
            accessor.skyocean$setNpc(isNpc);
        }
    }

    void skyocean$setSelf(boolean isSelf);

    boolean skyocean$isSelf();

    void skyocean$setNpc(boolean isNpc);

    boolean skyocean$isNpc();

}
