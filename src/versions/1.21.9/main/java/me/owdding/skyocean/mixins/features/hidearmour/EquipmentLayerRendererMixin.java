package me.owdding.skyocean.mixins.features.hidearmour;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import me.owdding.skyocean.accessors.hidearmour.ItemStackAccessor;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

    @Unique
    private static final RenderType TRANSPARENT_TRIMS_TYPE = RenderType.armorTranslucent(Sheets.ARMOR_TRIMS_SHEET);

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
        )
    )
    public RenderType modifyRenderType(
        ResourceLocation location,
        Operation<RenderType> original,
        @Share(value = "translucent", namespace = "skyocean") LocalRef<Boolean> renderTranslucent
    ) {
        if (renderTranslucent.get() != null && renderTranslucent.get()) {
            return RenderType.armorTranslucent(location);
        }
        return original.call(location);
    }

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Sheets;armorTrimsSheet(Z)Lnet/minecraft/client/renderer/RenderType;"
        )
    )
    public RenderType modifyTrimsRenderType(
        boolean $$0,
        Operation<RenderType> original,
        @Share(value = "translucent", namespace = "skyocean") LocalRef<Boolean> renderTranslucent
    ) {
        if (renderTranslucent.get() != null && renderTranslucent.get()) {
            return TRANSPARENT_TRIMS_TYPE;
        }
        return original.call($$0);
    }

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;getColorForLayer(Lnet/minecraft/client/resources/model/EquipmentClientInfo$Layer;I)I"
        )
    )
    public int renderLayers(
        EquipmentClientInfo.Layer layer,
        int defaultColor,
        Operation<Integer> original,
        @Local(argsOnly = true) ItemStack stack,
        @Share(value = "translucent", namespace = "skyocean") LocalRef<Boolean> renderTranslucent
    ) {
        var color = original.call(layer, defaultColor);
        var alpha = ItemStackAccessor.getAlpha(stack);
        if (alpha != null && color != null && alpha < 253) {
            renderTranslucent.set(true);
            return (alpha << 24) | (color & 0xFFFFFF);
        }

        return color != null ? color : -1;
    }

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            ordinal = 2
        )
    )
    public <S> void renderTrimToBuffer(
        OrderedSubmitNodeCollector instance,
        Model<? super S> model,
        S s,
        PoseStack poseStack,
        RenderType renderType,
        int i,
        int i2,
        int i3,
        TextureAtlasSprite textureAtlasSprite,
        int i4,
        ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
        Operation<Void> original,
        @Local(argsOnly = true) ItemStack stack
    ) {
        var alpha = ItemStackAccessor.getAlpha(stack);
        if (alpha != null) {
            original.call(instance, model, s, poseStack, renderType, i, i2, (alpha << 24) | i3, textureAtlasSprite, i4, crumblingOverlay);
        } else {
            original.call(instance, model, s, poseStack, renderType, i, i2, i3, textureAtlasSprite, i4, crumblingOverlay);
        }
    }
}
