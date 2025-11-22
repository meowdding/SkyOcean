package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyedItemColor.class)
public class DyedItemColorMixin {

    @WrapOperation(method = "getOrDefault", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private static <T> T replaceDyeColor(ItemStack instance, DataComponentType<T> type, Operation<T> original) {
        return CustomItemsHelper.replace(instance, type, original);
    }

}
