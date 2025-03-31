package codes.cookies.skyocean.mixins;

import codes.cookies.skyocean.LanguageHelper;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Component.class)
public interface ComponentMixin {

    @Unique
    private static void handle(String key, String fallback, Object[] args, CallbackInfoReturnable<MutableComponent> cir) {
        if (!LanguageHelper.ALLOW_TAGS.contains(key)) {
            return;
        }

        String orDefault = Language.getInstance().getOrDefault(key, fallback);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            orDefault = orDefault.replaceAll("<" + i + ">", String.valueOf(arg));
        }
        final Component component = TagParser.QUICK_TEXT_SAFE.parseText(orDefault, ParserContext.of());
        cir.setReturnValue((MutableComponent) component);
    }

    @Inject(method = "translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
    private static void translatable(String key, CallbackInfoReturnable<MutableComponent> cir) {
        handle(key, null, TranslatableContents.NO_ARGS, cir);
    }
    @Inject(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
    private static void translatable(String key, Object[] args, CallbackInfoReturnable<MutableComponent> cir) {
        handle(key, null, args, cir);
    }
    @Inject(method = "translatableWithFallback(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
    private static void translatable(String key, String fallback, CallbackInfoReturnable<MutableComponent> cir) {
        handle(key, fallback, TranslatableContents.NO_ARGS, cir);
    }
    @Inject(method = "translatableWithFallback(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
    private static void translatable(String key, String fallback, Object[] args, CallbackInfoReturnable<MutableComponent> cir) {
        handle(key, fallback, args, cir);
    }

}
