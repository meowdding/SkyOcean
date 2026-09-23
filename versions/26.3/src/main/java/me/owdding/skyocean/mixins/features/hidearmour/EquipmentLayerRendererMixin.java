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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
        )
    )
    public RenderType modifyRenderType(
        Identifier location,
        Operation<RenderType> original,
        @Share(value = "translucent", namespace = "skyocean") LocalRef<Boolean> renderTranslucent
    ) {
        if (renderTranslucent.get() != null && renderTranslucent.get()) {
            return RenderTypes.entityTranslucent(location);
        }
        return original.call(location);
    }

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorTrim(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;"
        )
    )
    public RenderType modifyTrimsRenderType(
        Identifier identifier,
        boolean decal,
        Operation<RenderType> original,
        @Share(value = "translucent", namespace = "skyocean") LocalRef<Boolean> renderTranslucent
    ) {
        if (renderTranslucent.get() != null && renderTranslucent.get()) {
            return RenderTypes.entityTranslucent(identifier);
        }
        return original.call(identifier, decal);
    }

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
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
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V",
            ordinal = 1
        )
    )
    public <S> void renderTrimToBuffer(
        OrderedSubmitNodeCollector instance,
        Model<? super S> model,
        S state, PoseStack poseStack,
        RenderType renderType,
        int lightCoords,
        int overlayCoords,
        int tintedColor,
        @Nullable UvMapping uvMapping,
        int outlineColor,
        Operation<Void> original,
        @Local(argsOnly = true) ItemStack stack
    ) {
        var alpha = ItemStackAccessor.getAlpha(stack);
        if (alpha != null) {
            original.call(instance, model, state, poseStack, renderType, lightCoords, overlayCoords, (alpha << 24) | (tintedColor  & 0xFFFFFF),  uvMapping, outlineColor);
        } else {
            original.call(instance, model, state, poseStack, renderType, lightCoords, overlayCoords, tintedColor, uvMapping, outlineColor);
        }
    }
}
