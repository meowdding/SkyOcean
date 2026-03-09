package me.owdding.skyocean.utils.debug

import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.chat.ChatUtils
import tech.thatgravyboat.skyblockapi.api.events.misc.AbstractModRegisterDebugEvent

internal class RegisterSkyOceanDebugEvent(base: RegisterSkyOceanCommandEvent) :
    AbstractModRegisterDebugEvent(ChatUtils.prefix, true, base)
