package me.owdding.skyocean.mixins;

import me.owdding.skyocean.accessors.AvatarRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements AvatarRenderStateAccessor {
    @Unique
    private UUID ocean$uuid;
    @Unique
    private boolean skyocean$isSelf;
    @Unique
    private boolean skyocean$isNpc;
    @Unique
    private LivingEntityRenderState skyocean$animalState;

    @Override
    public UUID ocean$getUUID() {
        return this.ocean$uuid;
    }

    @Override
    public void ocean$setUUID(UUID uuid) {
        this.ocean$uuid = uuid;
    }


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

    @Override
    public LivingEntityRenderState skyocean$getAnimalState() {
        return skyocean$animalState;
    }

    @Override
    public void skyocean$setAnimalState(LivingEntityRenderState state) {
        this.skyocean$animalState = state;
    }
}
