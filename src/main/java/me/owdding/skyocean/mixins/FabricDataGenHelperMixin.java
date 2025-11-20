package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.events.DatagenFinishEvent;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI;

@Pseudo
@Mixin(value = FabricDataGenHelper.class, remap = false)
public class FabricDataGenHelperMixin {

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/datagen/FabricDataGenHelper;runInternal()V"))
    private static void postEvent(Operation<Void> original) {
        original.call();
        DatagenFinishEvent.INSTANCE.post(SkyBlockAPI.getEventBus());
    }

}

