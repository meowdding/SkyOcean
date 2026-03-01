package me.owdding.skyocean.mixins.features;

import me.owdding.skyocean.events.ParticleEmitEvent;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

@Mixin(ParticleEngine.class)
public class ParticalEngineMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    public void add(Particle effect, CallbackInfo ci) {
        if (new ParticleEmitEvent(effect).post(SkyBlockAPI.getEventBus())) {
            ci.cancel();
        }
    }

}
