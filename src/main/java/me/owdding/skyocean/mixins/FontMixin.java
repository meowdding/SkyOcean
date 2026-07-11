package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.config.features.misc.MiscConfig;
import me.owdding.skyocean.config.features.text_replacements.TextReplacementConfig;
import me.owdding.skyocean.features.text.MarkdownChat;
import me.owdding.skyocean.features.text.TextReplacements;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Font.class)
public class FontMixin {

    @WrapOperation(method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/FormattedCharSequence;accept(Lnet/minecraft/util/FormattedCharSink;)Z"))
    public boolean prepareText(FormattedCharSequence instance, FormattedCharSink formattedCharSink, Operation<Boolean> original) {
        if (!TextReplacementConfig.isEnabled()) {
            return original.call(instance, formattedCharSink);
        }

        return original.call(TextReplacements.apply(instance), formattedCharSink);
    }

    @WrapOperation(method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringDecomposer;iterateFormatted(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z"))
    public boolean prepareText(String text, Style style, FormattedCharSink sink, Operation<Boolean> original) {
        if (!TextReplacementConfig.isEnabled()) {
            return original.call(text, style, sink);
        }

        var replacements = TextReplacements.wrapSink(sink);
        original.call(text, style, replacements.getFirst());
        return replacements.getSecond().get();
    }

    @WrapOperation(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/StringSplitter;stringWidth(Lnet/minecraft/network/chat/FormattedText;)F"))
    public float width(StringSplitter instance, FormattedText content, Operation<Float> original) {
        if (!TextReplacementConfig.isEnabled()) {
            return original.call(instance, content);
        }

        return original.call(instance, MarkdownChat.INSTANCE.toComponent(TextReplacements.apply(Language.getInstance().getVisualOrder(content))));
    }

    @WrapOperation(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/StringSplitter;stringWidth(Lnet/minecraft/util/FormattedCharSequence;)F"))
    public float width(StringSplitter instance, FormattedCharSequence content, Operation<Float> original) {
        if (!TextReplacementConfig.isEnabled()) {
            return original.call(instance, content);
        }

        return original.call(instance, TextReplacements.apply(content));
    }

    @WrapOperation(method = "width(Ljava/lang/String;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/StringSplitter;stringWidth(Ljava/lang/String;)F"))
    public float width(StringSplitter instance, String content, Operation<Float> original) {
        if (!TextReplacementConfig.isEnabled()) {
            return original.call(instance, content);
        }

        return original.call(instance, TextReplacements.apply(content));
    }

}
