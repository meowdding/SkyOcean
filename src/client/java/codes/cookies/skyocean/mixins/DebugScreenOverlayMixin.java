package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.helper.SbEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Inject(method = "getSystemInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"))
    public void getSystemInfo(CallbackInfoReturnable<List<String>> cir, @Local List<String> list, @Local Entity entity) {
        list.add(String.valueOf(SbEntity.getAttachedLines(entity).size()));
        for (Component attachedLine : SbEntity.getAttachedLines(entity)) {
            list.add(attachedLine.getString());
        }
    }

}
