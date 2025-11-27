package me.owdding.skyocean.mixins;

import me.owdding.skyocean.features.inventory.buttons.InvButtons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(
        method = "renderBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"
        )
    )
    public void onBackgroundDrawEnd(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        InvButtons.INSTANCE.onScreenBackgroundAfter((AbstractContainerScreen<?>) (Object) this, graphics);
    }
}
