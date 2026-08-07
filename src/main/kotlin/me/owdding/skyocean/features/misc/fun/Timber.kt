package me.owdding.skyocean.features.misc.`fun`

import me.owdding.skyocean.config.features.misc.`fun`.FunConfig
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderHudEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient

@Module
object Timber {

    private val logger = MeowddingLogger.autoResolve()

    private val texture get() = FunConfig.timberTexture.texture
    private val sound get() = FunConfig.timberSound.soundId

    private val group = RemoteStrings.resolve()
    private val treeFellRegex by group.regex("(?:TIMBER|PETALFALL|WOODPECKER)! You felled the entire Tree!")

    private var currentInstant = 0L

    @Subscription
    fun onMessage(event: ChatReceivedEvent.Pre) {
        if (!FunConfig.timberSilly) return

        val text = event.text
        if (!treeFellRegex.matches(text)) return

        currentInstant = System.currentTimeMillis()

        val soundId = sound ?: return
        val instance = SimpleSoundInstance.forUI(
            SoundEvent.createVariableRangeEvent(soundId),
            1f,
            1f,
        )
        try {
            McClient.self.soundManager.play(instance)
        } catch (e: Exception) {
            logger.error("Failed to play $soundId sound", e)
        }
    }

    @Subscription
    fun render(event: RenderHudEvent) {
        if (!FunConfig.timberSilly) return
        val graphics = event.graphics
        val time = (System.currentTimeMillis() - currentInstant)
        val opacity = when {
            time > 700 -> return
            else -> 1.0f - (time - 400).coerceAtLeast(0) / 300f
        }
        if (opacity > 0.0f) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, 0, 0, graphics.guiWidth(), graphics.guiHeight(), opacity)
        }
    }
}

enum class TimberTextureOption(val texture: Identifier) {
    SUGARCOAT(SkyOcean.id("fun/timber")),
    FLASHBANG(SkyOcean.id("fun/flashbang")),
}

enum class TimberSoundOption(val soundId: Identifier?) {
    TIMBER(SkyOcean.id("timber.timber_sound")),
    SUGARCOAT(SkyOcean.id("timber.sugarcoat_sound")),
    FLASHBANG(SkyOcean.id("timber.flashbang_sound")),
    NONE(null),
}
