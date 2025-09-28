package me.owdding.skyocean.mixins.features.customize;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.features.item.custom.CustomItemsHelper;
import net.minecraft.Optionull;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
        )
    )
    public <T> T replaceArmorTrim(ItemStack instance, DataComponentType<T> type, Operation<T> original) {
        return CustomItemsHelper.replace(instance, type, original);
    }

    @WrapMethod(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V"
    )
    public void replaceArmorTrim(
        EquipmentClientInfo.LayerType $$0,
        ResourceKey<EquipmentAsset> asset,
        Model<?> $$2,
        Object $$3,
        ItemStack stack,
        PoseStack $$5,
        SubmitNodeCollector $$6,
        int $$7,
        ResourceLocation $$8,
        int $$9,
        int $$10,
        Operation<Void> original
    ) {
        var equippable = Optionull.map(
            CustomItemsHelper.getData(stack, DataComponents.EQUIPPABLE),
            it -> it.assetId().orElse(null)
        );
        original.call($$0, equippable != null ? equippable : asset, $$2, $$3, stack, $$5, $$6, $$7, $$8, $$9, $$10);
    }

}
