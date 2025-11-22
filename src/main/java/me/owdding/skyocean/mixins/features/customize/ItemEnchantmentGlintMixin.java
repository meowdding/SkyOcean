package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import me.owdding.skyocean.utils.Utils;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = {BlockModelWrapper.class, SpecialModelWrapper.class})
public class ItemEnchantmentGlintMixin {

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"))
    public boolean update(ItemStack instance, Operation<Boolean> original) {
        return Boolean.TRUE.equals(Utils.nonNullElseGet(
            CustomItemsHelper.getData(instance, DataComponents.ENCHANTMENT_GLINT_OVERRIDE),
            () -> original.call(instance)));
    }

}
