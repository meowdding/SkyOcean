package me.owdding.skyocean.mixins.features.keybinds;

import me.owdding.skyocean.features.hotkeys.system.HotkeyManager;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
class GuiMixin {

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;releaseAll()V"))
    public void releaseAll(CallbackInfo ci) {
        HotkeyManager.releaseAll();
    }

}
