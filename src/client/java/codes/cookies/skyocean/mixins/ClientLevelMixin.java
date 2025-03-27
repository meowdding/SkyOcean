package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.api.event.ListenForNameChange;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "addEntity", at = @At("HEAD"))
    public void addEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof ArmorStand) {
            ((ListenForNameChange) entity).ocaen$markAsNameTag();
        }
    }

}
