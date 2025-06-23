package me.owdding.skyocean.mixins;

import me.owdding.skyocean.events.SoundPlayedEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;
import tech.thatgravyboat.skyblockapi.helpers.McClient;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(
        method = "handleSoundEvent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void onSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener) (Object) this, McClient.INSTANCE.getSelf());
        SoundPlayedEvent soundEvent = new SoundPlayedEvent(
            packet.getSound().value(),
            new Vec3(packet.getX(), packet.getY(), packet.getZ()),
            packet.getVolume(),
            packet.getPitch()
        );
        soundEvent.post(SkyBlockAPI.getEventBus());
        if (soundEvent.isCancelled()) {
            ci.cancel();
        }
    }
}
