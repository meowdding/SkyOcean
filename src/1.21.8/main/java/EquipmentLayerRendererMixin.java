import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.owdding.skyocean.accessors.hidearmour.ItemStackAccessor;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
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
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
        )
    )
    public void renderToBuffer(
        Model instance, PoseStack pose, VertexConsumer consumer,
        int light, int overlay, Operation<Void> original,
        @Local(argsOnly = true) ItemStack stack
    ) {
        var alpha = ItemStackAccessor.getAlpha(stack);
        if (alpha != null) {
            instance.renderToBuffer(pose, consumer, light, overlay, ARGB.color(alpha, 0xFFFFFF));
            return;
        }
        original.call(instance, pose, consumer, light, overlay);
    }

    @WrapOperation(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
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
        if (alpha != null && color != null) {
            renderTranslucent.set(true);
            return (alpha << 24) | (color & 0xFFFFFF);
        }

        return color != null ? color : -1;
    }
}
