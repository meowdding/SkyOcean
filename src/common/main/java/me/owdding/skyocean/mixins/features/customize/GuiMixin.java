package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(Gui.class)
public class GuiMixin {

    @WrapOperation(method = {"renderSelectedItemName", "tick()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;"))
    public Component renderSelectedItemName(ItemStack instance, Operation<Component> original) {
        return Objects.requireNonNullElseGet(CustomItemsHelper.getData(instance, DataComponents.CUSTOM_NAME), () -> original.call(instance));
    }

}
