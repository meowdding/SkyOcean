//? > 1.21.10 {
package me.owdding.skyocean.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.owdding.skyocean.features.text.MarkdownChat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(GuiMessage.class)
public class ChatComponentMixin {

    @WrapOperation(method = "splitLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;"))
    public List<FormattedCharSequence> modify(FormattedText component, int maxWidth, Font font, Operation<List<FormattedCharSequence>> original) {
        return original.call(MarkdownChat.INSTANCE.tryModify(component), maxWidth, font);
    }

}
//? }
