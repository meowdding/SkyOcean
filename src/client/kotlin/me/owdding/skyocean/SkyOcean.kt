package me.owdding.skyocean

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.Config
import me.owdding.skyocean.generated.SkyOceanModules
import me.owdding.skyocean.helpers.fakeblocks.FakeBlocks
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.RepoVersion
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object SkyOcean : ClientModInitializer, Logger by LoggerFactory.getLogger("SkyOcean") {

    val SELF = FabricLoader.getInstance().getModContainer("skyocean").get()
    val MOD_ID = SELF.metadata.id
    val VERSION = SELF.metadata.version.friendlyString

    val configurator = Configurator("skyocean")

    override fun onInitializeClient() {
        Config.register(configurator)
        RepoAPI.setup(RepoVersion.V1_21_5)
        SkyOceanModules.init { SkyBlockAPI.eventBus.register(it) }

        PreparableModelLoadingPlugin.register(FakeBlocks::init, FakeBlocks)
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
