package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.AvatarRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(PlayerRenderState.class)
public class AvatarRenderStateMixin implements AvatarRenderStateAccessor {
    @Unique
    private UUID ocean$uuid;

    @Override
    public UUID ocean$getUUID() {
        return this.ocean$uuid;
    }

    @Override
    public void ocean$setUUID(UUID uuid) {
        this.ocean$uuid = uuid;
    }
}
