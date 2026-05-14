package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.features.text.TextReplacements;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Font.class)
public class FontMixin {

    @WrapOperation(method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/FormattedCharSequence;accept(Lnet/minecraft/util/FormattedCharSink;)Z"))
    public boolean prepareText(FormattedCharSequence instance, FormattedCharSink formattedCharSink, Operation<Boolean> original) {
        return original.call(TextReplacements.apply(instance), formattedCharSink);
    }

    @WrapOperation(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/StringSplitter;stringWidth(Lnet/minecraft/util/FormattedCharSequence;)F"))
    public float width(StringSplitter instance, FormattedCharSequence content, Operation<Float> original) {
        return original.call(instance, TextReplacements.apply(content));
    }

}
