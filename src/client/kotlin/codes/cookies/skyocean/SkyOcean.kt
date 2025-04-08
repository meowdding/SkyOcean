package codes.cookies.skyocean

import codes.cookies.skyocean.config.Config
import codes.cookies.skyocean.events.FakeBlockModelEvent
import codes.cookies.skyocean.generated.Modules
import codes.cookies.skyocean.helpers.fakeblocks.FakeBlocks
import codes.cookies.skyocean.modules.Module
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.RepoVersion
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object SkyOcean : ClientModInitializer, Logger by LoggerFactory.getLogger("SkyOcean") {

    val SELF = FabricLoader.getInstance().getModContainer("skyocean").get()
    val VERSION = SELF.metadata.version.friendlyString

    val configurator = Configurator("skyocean")

    override fun onInitializeClient() {
        Config.register(configurator)
        RepoAPI.setup(RepoVersion.V1_21_5)
        Modules.load()

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

    @Subscription
    fun replaceModels(event: FakeBlockModelEvent) {
        event.register(Blocks.STONE, id("test")) { _, pos ->
            pos.y < 0
        }
    }

    fun id(path: String) = ResourceLocation.fromNamespaceAndPath(SELF.metadata.id, path)
}
