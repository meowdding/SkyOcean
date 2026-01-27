package me.owdding.skyocean.mixins.compat.rei;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.owdding.skyocean.features.item.search.highlight.ReiItemHighlighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@IfModLoaded("roughlyenoughitems")
@Mixin(targets = "me.shedaniel.rei.impl.client.gui.widget.EntryHighlighter")
public class EntryHighlighterMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private static void stopRender(CallbackInfo ci) {
        if (ReiItemHighlighter.shouldStopREIHighlight()) ci.cancel();
    }
}
