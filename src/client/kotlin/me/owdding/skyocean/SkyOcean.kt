package me.owdding.skyocean

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingUpdateChecker
import me.owdding.skyocean.config.Config
import me.owdding.skyocean.generated.SkyOceanModules
import me.owdding.skyocean.helpers.fakeblocks.FakeBlocks
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.RepoVersion
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.url

@Module
object SkyOcean : ClientModInitializer, Logger by LoggerFactory.getLogger("SkyOcean") {

    val SELF = FabricLoader.getInstance().getModContainer("skyocean").get()
    val MOD_ID: String = SELF.metadata.id
    val VERSION: String = SELF.metadata.version.friendlyString

    val configurator = Configurator("skyocean")

    override fun onInitializeClient() {
        Config.register(configurator)
        RepoAPI.setup(RepoVersion.V1_21_5)
        MeowddingUpdateChecker("dIczrQAR", SELF, ::sendUpdateMessage)
        SkyOceanModules.init { SkyBlockAPI.eventBus.register(it) }

        PreparableModelLoadingPlugin.register(FakeBlocks::init, FakeBlocks)
    }

    fun sendUpdateMessage(link: String, current: String, new: String) {
        fun MutableComponent.withLink() = this.apply {
            this.url = link
            this.hover = Text.of(link).withColor(TextColor.GRAY)
        }

        McClient.tell {
            Text.of().send()
            Text.join(
                "New version found! (",
                Text.of(current).withColor(TextColor.RED),
                Text.of(" -> ").withColor(TextColor.GRAY),
                Text.of(new).withColor(TextColor.GREEN),
                ")",
            ).withLink().sendWithPrefix()
            Text.of("Click to download.").withLink().sendWithPrefix()
            Text.of().send()
        }
    }

    @Subscription
    fun onCommand(event: RegisterCommandsEvent) {
        event.register("skyocean") {
            this.callback {
                McClient.tell {
                    McClient.setScreen(ResourcefulConfigScreen.getFactory("skyocean").apply(null))
                }
            }
        }
    }


    fun id(path: String) = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
}
