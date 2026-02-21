package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import me.owdding.skyocean.utils.Utils;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @WrapOperation(
        method = {
            "shouldRender(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z",
            "renderArmorPiece"
        },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
    )
    private static <T> T get(ItemStack instance, DataComponentType<T> dataComponentType, Operation<T> original) {
        return Utils.nonNullElseGet(CustomItemsHelper.getData(instance, dataComponentType), () -> original.call(instance, dataComponentType));
    }

    @WrapOperation(
        method = "shouldRender(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;shouldRender(Lnet/minecraft/world/item/equipment/Equippable;Lnet/minecraft/world/entity/EquipmentSlot;)Z")
    )
    private static boolean replaceArmorTrim(Equippable equippable, EquipmentSlot slot, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack) {
        var state = CustomItemsHelper.getEquippableState(stack);
        return switch (state.getState()) {
            case DEFAULT -> original.call(equippable, slot) && CustomItemsHelper.getData(stack, DataComponents.PROFILE) == null;
            case TRUE -> original.call(state.getEquippable(), slot);
            case FALSE -> false;
        };
    }
}
