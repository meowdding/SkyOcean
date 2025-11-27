package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockModelWrapper.class)
public class BlockModelWrapperMixin {

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/item/ItemTintSource;calculate(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/LivingEntity;)I"))
    public int modifyColor(
        ItemTintSource instance,
        ItemStack itemStack,
        ClientLevel clientLevel,
        LivingEntity livingEntity,
        Operation<Integer> original,
        @Share("foundFirst") LocalBooleanRef foundFirst
    ) {
        var customColor = CustomItemsHelper.getColor(itemStack);

        if (instance instanceof Constant || customColor == null || foundFirst.get()) {
            return original.call(instance, itemStack, clientLevel, livingEntity);
        }

        foundFirst.set(true);
        return ARGB.opaque(customColor);
    }

}
