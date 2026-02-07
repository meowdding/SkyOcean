package me.owdding.skyocean.mixins;

import kotlin.time.Instant;
import me.owdding.skyocean.accessors.AvatarRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements AvatarRenderStateAccessor {
    @Unique
    private UUID ocean$uuid;
    @Unique
    private boolean skyocean$isSelf;
    @Unique
    private boolean skyocean$isNpc;
    @Unique
    private LivingEntityRenderState skyocean$animalState;
    @Unique
    private ItemStack skyocean$heldItemStack;
    @Unique
    private Instant skyocean$lastMoveTime;

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

    @Override
    public void ocean$setHeldItemStack(ItemStack stack) {
        this.skyocean$heldItemStack = stack;
    }

    @Override
    public ItemStack ocean$getHeldItemStack() {
        return this.skyocean$heldItemStack;
    }

    @Override
    public Instant ocean$getLastMoveTime() {
        return this.skyocean$lastMoveTime;
    }

    @Override
    public void ocean$setLastMoveTime(Instant time) {
        this.skyocean$lastMoveTime = time;
    }
}
