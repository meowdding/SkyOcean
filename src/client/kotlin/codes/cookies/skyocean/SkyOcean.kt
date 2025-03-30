package codes.cookies.skyocean

import codes.cookies.skyocean.config.Config
import codes.cookies.skyocean.generated.Modules
import codes.cookies.skyocean.modules.Module
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import net.fabricmc.api.ClientModInitializer
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.repolib.api.RepoVersion
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object SkyOcean : ClientModInitializer {

    val configurator = Configurator("skyocean")

    override fun onInitializeClient() {
        Config.register(configurator)
        RepoAPI.setup(RepoVersion.V1_21_5)
        Modules.load()
    }


    @Subscription
    fun commands(event: RegisterCommandsEvent) {
        event.register("skyocean") {
            this.callback {
                McClient.tell {
                    McClient.setScreen(ResourcefulConfigScreen.getFactory("skyocean").apply(null))
                }
            }
        }
    }
}
