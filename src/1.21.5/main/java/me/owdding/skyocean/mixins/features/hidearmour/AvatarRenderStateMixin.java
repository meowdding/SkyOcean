package me.owdding.skyocean.mixins.features.hidearmour;

import me.owdding.skyocean.accessors.hidearmour.PlayerRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class AvatarRenderStateMixin implements PlayerRenderStateAccessor {

    @Unique
    private boolean skyocean$isSelf;
    @Unique
    private boolean skyocean$isNpc;

    @Override
    public void skyocean$setSelf(boolean isSelf) {
        this.skyocean$isSelf = isSelf;
    }

    @Override
    public boolean skyocean$isSelf() {
        return this.skyocean$isSelf;
    }

    @Override
    public void skyocean$setNpc(boolean isNpc) {
        this.skyocean$isNpc = isNpc;
    }

    @Override
    public boolean skyocean$isNpc() {
        return this.skyocean$isNpc;
    }
}
