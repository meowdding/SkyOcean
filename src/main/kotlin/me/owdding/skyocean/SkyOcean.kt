package me.owdding.skyocean

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import me.owdding.ktmodules.Module
import me.owdding.lib.compat.RemoteConfig
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.lib.overlays.EditOverlaysScreen
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingUpdateChecker
import me.owdding.repo.RemoteRepo
import me.owdding.skyocean.config.Config
import me.owdding.skyocean.generated.SkyOceanLateInitModules
import me.owdding.skyocean.generated.SkyOceanModules
import me.owdding.skyocean.generated.SkyOceanPreInitModules
import me.owdding.skyocean.helpers.MixinHelper
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.HolderLookup
import net.minecraft.data.registries.VanillaRegistries
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.RepoStatusEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.url
import kotlin.jvm.optionals.getOrNull

@Module
object SkyOcean : ClientModInitializer, MeowddingLogger by MeowddingLogger.autoResolve() {

    private var meowddingRepo: Boolean = false
    private var apiRepo: Boolean = false

    val registryLookup: HolderLookup.Provider by lazy { VanillaRegistries.createLookup() }
    val SELF = FabricLoader.getInstance().getModContainer("skyocean").get()
    val DATAGEN_SELF by lazy { FabricLoader.getInstance().getModContainer("skyocean-datagen").getOrNull() }
    val SBAPI by lazy { FabricLoader.getInstance().getModContainer(SkyBlockAPI.MOD_ID).get() }
    val MOD_ID: String = SELF.metadata.id
    val VERSION: String = SELF.metadata.version.friendlyString
    const val DISCORD = "https://meowdd.ing/discord"

    init {
        SkyOceanPreInitModules.init {
            SkyBlockAPI.eventBus.register(it)
        }
    }

    val configurator = Configurator("skyocean")
    val config = Config.register(configurator)

    override fun onInitializeClient() {
        MixinHelper.isStarted = true
        RemoteConfig.lockConfig(Config.register(configurator), "https://remote-configs.owdding.me/skyocean.json", SELF)
        MeowddingUpdateChecker("dIczrQAR", SELF, ::sendUpdateMessage)
        SkyOceanModules.init {
            SkyBlockAPI.eventBus.register(it)
        }

        apiRepo = RepoAPI.isInitialized()
        meowddingRepo = RemoteRepo.isInitialized()

        onRepoReady()
    }

    @Subscription
    private fun RepoStatusEvent.repoReady() {
        apiRepo = true
        onRepoReady()
    }

    @Subscription
    private fun FinishRepoLoadingEvent.repoReady() {
        meowddingRepo = true
        onRepoReady()
    }

    fun onRepoReady() {
        if (!apiRepo || !meowddingRepo) return
        SkyOceanLateInitModules.collected.forEach { SkyBlockAPI.eventBus.register(it) }
    }

    fun sendUpdateMessage(link: String, current: String, new: String) {
        fun MutableComponent.withLink() = this.apply {
            this.url = link
            this.hover = Text.of(link).withColor(TextColor.GRAY)
        }

        McClient.runNextTick {
            Text.of().send()
            Text.join(
                "New version found! (",
                Text.of(current, TextColor.RED),
                Text.of(" -> ", TextColor.GRAY),
                Text.of(new, TextColor.GREEN),
                ")",
            ).withLink().sendWithPrefix()
            Text.of("Click to download.").withLink().sendWithPrefix()
            Text.of().send()
        }
    }

    @Subscription
    fun onCommand(event: RegisterCommandsEvent) {
        event.register("skyocean") {
            thenCallback("version") {
                Text.of("Version: $VERSION").withColor(TextColor.GRAY).sendWithPrefix()
            }

            thenCallback("discord") {
                Text.of("Join the Meowdding Discord!").apply {
                    this.url = DISCORD
                    this.hover = Text.of(DISCORD).withColor(TextColor.GRAY)
                }.sendWithPrefix()
            }

            thenCallback("overlays") {
                McClient.setScreenAsync { EditOverlaysScreen(MOD_ID) }
            }

            callback {
                McClient.setScreenAsync { ResourcefulConfigScreen.getFactory("skyocean").apply(null) }
            }
        }
    }

    fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)
    fun minecraft(path: String): Identifier = Identifier.withDefaultNamespace(path)
    fun olympus(path: String): Identifier = Identifier.fromNamespaceAndPath("olympus", path)
}
