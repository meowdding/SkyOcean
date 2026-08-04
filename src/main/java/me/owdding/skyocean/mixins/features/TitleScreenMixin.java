package me.owdding.skyocean.mixins.features;

import me.owdding.skyocean.config.features.misc.MiscConfig;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockapi.utils.text.Text;
import tech.thatgravyboat.skyblockapi.utils.text.TextColor;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private Button skyocean$quickJoinButton;

    @Unique
    private Button skyocean$multiplayerButton;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addQuickJoinButton(CallbackInfo ci) {
        if (!MiscConfig.INSTANCE.getQuickJoinButton()) {
            return;
        }

        Button multiplayerButton = null;
        for (AbstractWidget widget : Screens.getWidgets(this)) {
            if (widget instanceof Button button && button.getMessage().getContents() instanceof TranslatableContents contents) {
                if ("menu.multiplayer".equals(contents.getKey())) {
                    multiplayerButton = button;
                    break;
                }
            }
        }

        if (multiplayerButton != null) {
            multiplayerButton.setWidth(98);

            String serverIp = MiscConfig.INSTANCE.getQuickJoinIp();
            String text = MiscConfig.INSTANCE.getQuickJoinText().replace("{ip}", serverIp);
            this.skyocean$quickJoinButton = Button.builder(Component.literal(text), button -> {
                    ServerData serverData = new ServerData("Quick Join", serverIp, ServerData.Type.OTHER);
                    // Disables the Resourcepack Prompt as this never gets saved to disk (its not in the serverlist)
                    serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                    ServerAddress serverAddress = ServerAddress.parseString(serverIp);
                    ConnectScreen.startConnecting(this, this.minecraft, serverAddress, serverData, false, null);
                })
                .bounds(multiplayerButton.getX() + 102, multiplayerButton.getY(), 98, 20)
                .tooltip(Tooltip.create(Text.INSTANCE.of("Added by SkyOcean. Customize it in Config", TextColor.GRAY)))
                .build();

            this.skyocean$multiplayerButton = multiplayerButton;
            Screens.getWidgets(this).add(this.skyocean$quickJoinButton);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void enforceQuickJoinPosition(CallbackInfo ci) {
        if (this.skyocean$quickJoinButton != null && this.skyocean$multiplayerButton != null) {
            this.skyocean$quickJoinButton.setY(this.skyocean$multiplayerButton.getY());
            this.skyocean$quickJoinButton.setX(this.skyocean$multiplayerButton.getX() + 102);
        }
    }
}
