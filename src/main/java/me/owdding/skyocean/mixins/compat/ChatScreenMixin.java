package me.owdding.skyocean.mixins.compat;

import me.owdding.skyocean.features.hotkeys.IgnoreHotkeyInputs;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatScreen.class)
public class ChatScreenMixin implements IgnoreHotkeyInputs {
}
