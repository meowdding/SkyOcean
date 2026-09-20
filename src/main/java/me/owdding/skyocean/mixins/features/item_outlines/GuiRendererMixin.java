package me.owdding.skyocean.mixins.features.item_outlines;

//~ if >= 26.2 'Matrix3x2f' -> 'Matrix3x2fc' {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Set;
import me.owdding.skyocean.features.item.RarityOutlines;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.thatgravyboat.skyblockapi.helpers.McClient;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @WrapOperation(
        method = "submitBlitFromItemAtlas",
        at = @At(
            value = "NEW",
            target = "net/minecraft/client/renderer/state/gui/BlitRenderState"
        )
    )
    private BlitRenderState modify(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2fc pose,
        int x0,
        int y0,
        int x1,
        int y1,
        float u0,
        float u1,
        float v0,
        float v1,
        int color,
        ScreenRectangle scissorArea,
        ScreenRectangle bounds,
        Operation<BlitRenderState> original,
        @Local(argsOnly = true)
        GuiItemRenderState itemState
    ) {
        return original.call(RarityOutlines.createPipeline(itemState, pipeline), textureSetup, pose, x0, y0, x1, y1, u0, u1, v0, v1, color, scissorArea, bounds);
    }

    @Inject(
        method = "prepareItemAtlas",
        at = @At("RETURN")
    )
    private void onPrepareAtlas(Set<Object> itemsInFrame, int slotTextureSize, CallbackInfoReturnable<GuiItemAtlas> cir) {
        RarityOutlines.Buffer.INSTANCE.update(cir.getReturnValue().textureSize(), slotTextureSize, McClient.INSTANCE.getOptions().guiScale().get());
    }

    @Inject(
        method = "close",
        at = @At("TAIL")
    )
    private void onClose(CallbackInfo ci) {
        var buffer = RarityOutlines.Buffer.getGpuBuffer();
        if (buffer != null) {
            buffer.close();
        }
    }
}
//~}
