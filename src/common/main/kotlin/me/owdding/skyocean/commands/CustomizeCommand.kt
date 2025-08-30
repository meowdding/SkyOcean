package me.owdding.skyocean.commands

import me.owdding.ktmodules.Module
import me.owdding.lib.rendering.text.withTextShader
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.data.CustomItemDataComponents
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.OceanColors
import me.owdding.skyocean.utils.OceanGradients
import me.owdding.skyocean.utils.Utils.getArgument
import me.owdding.skyocean.utils.Utils.text
import net.minecraft.commands.arguments.ComponentArgument
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.style

@Module
object CustomizeCommand {

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("customize") {
            thenCallback(name = "name name", argument = ComponentArgument.textComponent(context())) {
                val name = getArgument<Component>("name")!!.copy().apply {
                    style { withTextShader(OceanGradients.TRANS) }
                }
                val item = McClient.self.player?.mainHandItem?.takeUnless { it.isEmpty } ?: run {
                    text("Not holding any item!") {
                        this.color = OceanColors.WARNING
                    }.sendWithPrefix()
                    return@thenCallback
                }

                val success = CustomItems.modify(item) {
                    this[CustomItemDataComponents.NAME] = name
                }

                if (success) {
                    text("Renamed item to ") {
                        append(name)
                        append("!")
                    }.sendWithPrefix()
                } else {
                    text("Unable to rename item!") {
                        this.color = OceanColors.WARNING
                    }.sendWithPrefix()
                }
            }
        }
    }

}
