package me.owdding.skyocean.mixins.features.keybinds;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import me.owdding.skyocean.features.hotkeys.system.HotkeyManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Definition(id = "screen", local = @Local(type = Screen.class))
    @Expression("screen != null")
    @Inject(method = "keyPress", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 3), cancellable = true)
    public void meow(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (HotkeyManager.handle(action, event)) {
            ci.cancel();
        }
    }

}
