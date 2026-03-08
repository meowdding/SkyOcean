package me.owdding.skyocean.mixins.features.keybinds;

import me.owdding.skyocean.features.hotkeys.system.HotkeyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;releaseAll()V"))
    public void releaseAll(Screen guiScreen, CallbackInfo ci) {
        HotkeyManager.releaseAll();
    }

}
