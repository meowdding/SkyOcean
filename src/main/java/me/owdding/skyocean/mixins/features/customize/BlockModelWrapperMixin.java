package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
//~ if >= 26.1 'BlockModelWrapper' -> 'CuboidItemModelWrapper'
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//~ if >= 26.1 'BlockModelWrapper' -> 'CuboidItemModelWrapper'
@Mixin(CuboidItemModelWrapper.class)
public class BlockModelWrapperMixin {

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/item/ItemTintSource;calculate(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/LivingEntity;)I"))
    public int modifyColor(
        ItemTintSource instance,
        ItemStack itemStack,
        ClientLevel clientLevel,
        LivingEntity livingEntity,
        Operation<Integer> original,
        @Share("foundFirst") LocalBooleanRef foundFirst,
        @Local(argsOnly = true) LocalRef<ItemStack> itemStackRef
        ) {
        var customColor = CustomItemsHelper.getColor(itemStack);
        var model = CustomItemsHelper.getCustomData(itemStack, CustomItemDataComponents.model());
        if (model != null) {
            var resolved = model.resolveToItem();
            if (resolved != null) itemStackRef.set(resolved.getDefaultInstance());
        }

        if (instance instanceof Constant || customColor == null || foundFirst.get()) {
            return original.call(instance, itemStack, clientLevel, livingEntity);
        }

        foundFirst.set(true);
        return ARGB.opaque(customColor);
    }

    //? if = 1.21.11 {
    /*@WrapOperation(method = "method_76557", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private static Item wrap(ItemStack instance, Operation<Item> original) {
        var model = CustomItemsHelper.getCustomData(instance, CustomItemDataComponents.model());
        if (model != null) {
            return model.resolveToItem();
        }

        return original.call(instance);
    }
    *///?}

}
