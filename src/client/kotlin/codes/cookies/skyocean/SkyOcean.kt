package codes.cookies.skyocean

import codes.cookies.skyocean.config.Config
import codes.cookies.skyocean.events.RenderWorldEvent
import codes.cookies.skyocean.generated.Modules
import codes.cookies.skyocean.helpers.fakeblocks.FakeBlocks
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.boundingboxes.DwarvenMinesBB
import codes.cookies.skyocean.utils.boundingboxes.OctreeDebugRenderer
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
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

    private var isInitialized = false

    override fun onInitializeClient() {
        if (isInitialized) {
            return
        }
        isInitialized = true
        Config.register(configurator)
        RepoAPI.setup(RepoVersion.V1_21_5)
        Modules.load()

        PreparableModelLoadingPlugin.register(FakeBlocks::init, FakeBlocks)
    }

    val debug = OctreeDebugRenderer(DwarvenMinesBB.MIST)

    @Subscription
    fun debug(event: RenderWorldEvent) {
        debug.render(event)
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


    fun id(path: String) = ResourceLocation.fromNamespaceAndPath(SELF.metadata.id, path)
}
