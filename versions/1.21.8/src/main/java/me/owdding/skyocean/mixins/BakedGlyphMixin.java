package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BakedGlyph.class)
public abstract class BakedGlyphMixin {

    @WrapOperation(
        method = "renderChar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V", ordinal = 0)
    )
    public void onRenderShadow(
        BakedGlyph instance,
        boolean italic,
        float x,
        float y,
        float z,
        Matrix4f pose,
        VertexConsumer buffer,
        int color,
        boolean bold,
        int packedLight,
        Operation<Void> original,
        @Local(argsOnly = true) BakedGlyph.GlyphInstance glyph
    ) {
        if (MiscConfig.INSTANCE.getFullTextShadow()) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (j != 0 || k != 0) {
                        float xShadowOffset = glyph.shadowOffset() * j;
                        float yShadowOffset = glyph.shadowOffset() * k;
                        original.call(instance, italic, glyph.x() + xShadowOffset, glyph.y() + yShadowOffset, z, pose, buffer, color, bold, packedLight);
                    }
                }
            }
        } else {
            original.call(instance, italic, x, y, z, pose, buffer, color, bold, packedLight);
        }
    }

    @WrapOperation(
        method = "renderChar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V", ordinal = 1)
    )
    public void onRenderShadowBold(
        BakedGlyph instance,
        boolean italic,
        float x,
        float y,
        float z,
        Matrix4f pose,
        VertexConsumer buffer,
        int color,
        boolean bold,
        int packedLight,
        Operation<Void> original,
        @Local(argsOnly = true) BakedGlyph.GlyphInstance glyph
    ) {
        if (MiscConfig.INSTANCE.getFullTextShadow()) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (j != 0 || k != 0) {
                        float xShadowOffset = glyph.shadowOffset() * j;
                        float yShadowOffset = glyph.shadowOffset() * k;
                        original.call(
                            instance,
                            italic,
                            glyph.x() + glyph.boldOffset() + xShadowOffset,
                            glyph.y() + yShadowOffset,
                            z,
                            pose,
                            buffer,
                            color,
                            bold,
                            packedLight);
                    }
                }
            }
        } else {
            original.call(instance, italic, x, y, z, pose, buffer, color, bold, packedLight);
        }
    }
}
