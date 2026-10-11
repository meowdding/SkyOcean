package me.owdding.skyocean.utils

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.DebugToggle
import tech.thatgravyboat.skyblockapi.utils.DevUtils
import java.nio.file.Path
import kotlin.io.path.Path

internal fun debugToggle(path: String, description: String = path): DebugToggle {
    return DebugToggle(SkyOcean.id(path), description, SkyOceanDevUtils)
}


@Module
internal object SkyOceanDevUtils : DevUtils() {
    override val commandName: String = "skyocean toggle"
    override fun send(component: MutableComponent) = component.sendWithPrefix()
    override val namespace: String = "skyocean"
    override val supportsChangingDefault: Boolean = true
    override val path: Path = System.getProperty("skyocean.property_path")?.let { Path(it) } ?: McClient.config.resolve("skyocean.properties")

    init {
        loadFromProperties(path)
    }

    @Subscription
    private fun commandRegister(event: RegisterCommandsEvent) {
        super.onCommandRegister(event)
    }
}
