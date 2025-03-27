package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.api.event.ComponentAttachEvent;
import codes.cookies.skyocean.api.event.ListenForNameChange;
import codes.cookies.skyocean.api.event.NameChangedEvent;
import codes.cookies.skyocean.helper.EntityAttachmentAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Mixin(Entity.class)
public class EntityMixin implements ListenForNameChange, EntityAttachmentAccessor {

    @Unique
    boolean autoAttach = false;
    @Unique
    boolean isNameTag = false;
    @Unique
    private int cooldown = 20;
    @Shadow
    private Level level;
    @Unique
    private List<Entity> attached;
    @Unique
    private Entity attachedTo;

    @Inject(method = "setCustomName", at = @At("RETURN"))
    public void setCustomName(Component name, CallbackInfo ci) {
        if (isNameTag) {
            new NameChangedEvent(((Entity) (Object) this), name).post(SkyBlockAPI.getEventBus());
        }
    }

    @Override
    public void ocaen$markAsNameTag() {
        isNameTag = true;
    }

    @Override
    public void ocean$unmarkNameTag() {
        isNameTag = false;
    }

    @Override
    public boolean ocean$isNameTag() {
        return isNameTag;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        if (!autoAttach) {
            return;
        }

        if (cooldown-- < 0) {
            ocean$attachToClosest();
            cooldown = 20;
        }
    }

    @Override
    public void ocean$attachToClosest() {
        autoAttach = true;
        final List<Entity> entities = this.level.getEntities(self(), AABB.ofSize(self().position(), 3, 3, 3));
        entities.sort(Comparator.comparing(e -> e.distanceToSqr(self())));
        entities.removeIf(ArmorStand.class::isInstance);
        entities.removeIf(Objects::isNull);

        int index = entities.indexOf(attachedTo);
        if ((index != -1 && index < 2) || entities.isEmpty()) {
            return;
        }

        final Entity first = entities.getFirst();
        if (first == null) {
            return;
        }

        if (attachedTo != first && attachedTo != null) {
            ((EntityAttachmentAccessor) first).ocean$getAttachments().remove(self());
        }

        ((EntityAttachmentAccessor) first).ocean$getAttachments().add(self());
        new ComponentAttachEvent(self().getCustomName(), first).post(SkyBlockAPI.getEventBus());
        attachedTo = first;
    }

    @Override
    public @NotNull List<Entity> ocean$getAttachments() {
        if (attached == null) {
            attached = new ArrayList<>();
        }

        return attached;
    }

    @Inject(method = "onRemoval", at = @At("RETURN"))
    public void remove(CallbackInfo ci) {
        if (this.attachedTo != null) {
            ((EntityAttachmentAccessor) attachedTo).ocean$getAttachments().remove(self());
            this.autoAttach = false;
        }
    }

    @Unique
    private Entity self() {
        return (Entity) (Object) this;
    }
}
