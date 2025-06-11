package me.owdding.skyocean.mixins;

import me.owdding.skyocean.utils.ContainerBypass;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "mouseScrolled", at = @At("TAIL"), cancellable = true)
    void passScrollToParent(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.getChildAt(mouseX, mouseY)
            .filter((listener) -> listener instanceof ContainerBypass)
            .filter((guiEventListener) -> guiEventListener.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            .isPresent()
        );
    }
}
