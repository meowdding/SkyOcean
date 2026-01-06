package me.owdding.skyocean.mixins.features.customize;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
//? if < 1.21.11 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.At;
*///?}

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {

    //? if < 1.21.11 {
    /*@WrapOperation(method = "getRenderType(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/RenderType;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private static Item replaceItem(ItemStack instance, Operation<Item> original) {
        var model = CustomItemsHelper.getCustomData(instance, CustomItemDataComponents.model());
        if (model != null) {
            return model.resolveToItem();
        }

        return original.call(instance);
    }
    *///?}

}
