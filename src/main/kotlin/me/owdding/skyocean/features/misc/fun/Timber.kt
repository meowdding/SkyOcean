package me.owdding.skyocean.features.misc.`fun`

import me.owdding.skyocean.config.features.misc.`fun`.FunConfig
import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderHudEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.pushPop

@Module
object Timber {

    private val texture
        get() = when (FunConfig.timberTexture) {
            TimberTextureOption.SUGARCOAT -> SkyOcean.id("fun/timber")
            TimberTextureOption.FLASHBANG -> SkyOcean.id("fun/flashbang")
        }

    private val sound
        get() = when (FunConfig.timberSound) {
            TimberSoundOption.TIMBER -> SkyOcean.id("timber.timber_sound")
            TimberSoundOption.SUGARCOAT -> SkyOcean.id("timber.sugarcoat_sound")
            TimberSoundOption.FLASHBANG -> SkyOcean.id("timber.flashbang_sound")
        }

    private val triggers = listOf(
        "TIMBER! You felled the entire Tree!",
        "PETALFALL! You felled the entire Tree!",
        "WOODPECKER! You felled the entire Tree!",
    )

    private var timestamp = 0L

    @Subscription
    fun onMessage(event: ChatReceivedEvent.Pre) {
        if (!FunConfig.timberSilly) return

        val text = event.text
        if (triggers.none { text.contains(it) }) return

        timestamp = System.currentTimeMillis()
        val instance = SimpleSoundInstance.forUI(
            SoundEvent.createVariableRangeEvent(sound),
            1f,
            1f,
        )
        try {
            McClient.self.soundManager.play(instance)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscription
    fun render(event: RenderHudEvent) {
        if (!FunConfig.timberSilly) return
        val graphics = event.graphics
        val opacity = when (val time = System.currentTimeMillis() - timestamp) {
            in 0..400 -> 1.0f
            in 401..700 -> 1.0f - (time - 400) / 300.0f
            else -> 0.0f
        }
        if (opacity > 0.0f) {
            graphics.pushPop {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, 0, 0, graphics.guiWidth(), graphics.guiHeight(), opacity)
            }
        }
    }
}

enum class TimberTextureOption {
    SUGARCOAT,
    FLASHBANG,
}
enum class TimberSoundOption {
    TIMBER,
    SUGARCOAT,
    FLASHBANG,
}

