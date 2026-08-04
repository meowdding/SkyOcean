package me.owdding.skyocean.mixins.features.keybinds;

import me.owdding.skyocean.features.hotkeys.system.HotkeyManager;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    //~ if >= 26.2 'Minecraft;getOverlay' -> 'gui/Gui;overlay'
    @Inject(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;overlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0), cancellable = true)
    public void onButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (HotkeyManager.handle(rawButtonInfo, action)) {
            ci.cancel();
        }
    }

}
