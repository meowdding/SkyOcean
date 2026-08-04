package me.owdding.skyocean.mixins.features.keybinds;

import me.owdding.skyocean.features.hotkeys.system.HotkeyManager;
//? 26.1
//import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//~ if >= 26.2 'Minecraft' -> 'Gui' {
@Mixin(Gui.class)
class GuiMixin {
//~ }

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;releaseAll()V"))
    public void releaseAll(CallbackInfo ci) {
        HotkeyManager.releaseAll();
    }

}
