package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.PlayerRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements PlayerRenderStateAccessor {
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
