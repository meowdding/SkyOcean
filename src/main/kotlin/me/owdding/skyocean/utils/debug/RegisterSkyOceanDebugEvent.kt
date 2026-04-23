package me.owdding.skyocean.utils.debug

import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.chat.ChatUtils
import tech.thatgravyboat.skyblockapi.api.events.misc.AbstractModRegisterDebugEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text.send

internal class RegisterSkyOceanDebugEvent(base: RegisterSkyOceanCommandEvent) :
    AbstractModRegisterDebugEvent(ChatUtils.prefix, true, base) {

    fun oceanRegister(name: String, commandName: String, init: DebugBuilder.() -> Unit) {
        base.registerWithCallback(name(commandName)) {
            RootDebugBuilder(!name).apply(init).build().send()
        }
    }

}
