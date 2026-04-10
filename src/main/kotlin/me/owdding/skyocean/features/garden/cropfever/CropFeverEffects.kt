package me.owdding.skyocean.features.garden.cropfever

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.garden.CropFeverEffectsConfig
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.mixins.GameRendererAccessor
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyNonGuest
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.location.ServerDisconnectEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.GARDEN
import tech.thatgravyboat.skyblockapi.helpers.McClient
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.garden.cropfever.CoinRainOverlay.fallingCoinsList
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import kotlin.time.Instant
import me.owdding.skyocean.utils.nbs.NBSMusicManager

@Module
object CropFeverEffects {
    @JvmStatic
    var isFeverActive = false
        private set

    var startTime = Instant.DISTANT_PAST
    private const val BG_MUSIC_SOUND_ID = "farming.crop_fever.music"
    private const val START_SOUND_ID = "farming.crop_fever.start"
    private const val SHADER_ID = "crop_fever_hue_shift"

    private fun turnOff() {
        if (!isFeverActive) return
        isFeverActive = false

        startTime = Instant.DISTANT_PAST
        if (fallingCoinsList.isNotEmpty()) fallingCoinsList.clear()


        if (NBSMusicManager.isMusicActive(BG_MUSIC_SOUND_ID)) {
            NBSMusicManager.stop(BG_MUSIC_SOUND_ID)
        }
        if (NBSMusicManager.isMusicActive(START_SOUND_ID)) {
            NBSMusicManager.stop(START_SOUND_ID)
        }

        val gameRenderer = McClient.self.gameRenderer
        if (gameRenderer != null) {
            if (gameRenderer.currentPostEffect() == SkyOcean.id(SHADER_ID)) {
                gameRenderer.clearPostEffect()
            }
        }
    }

    private val group = RemoteStrings.resolve()
    private val startRegex by group.regex("^WOAH! You caught a case of the CROP FEVER for 60 seconds!")
    private val endRegex by group.regex("^GONE! Your CROP FEVER has been cured!")

    @Subscription
    @OnlyIn(GARDEN)
    @OnlyNonGuest
    fun onChatMessage(event: ChatReceivedEvent.Pre) {
        if (!CropFeverEffectsConfig.enabled) return
        if (startRegex.matches(event.text)) {
            if (isFeverActive) return

            isFeverActive = true

            if (CropFeverEffectsConfig.startingSound) {
                NBSMusicManager.play(START_SOUND_ID, "farming/crop_fever_start")
            }
            if (CropFeverEffectsConfig.backgroundMusic) {
                NBSMusicManager.play(BG_MUSIC_SOUND_ID, "farming/crop_fever_music")
            }
            if (CropFeverEffectsConfig.coinsDropping) {
                startTime = currentInstant()
            }
            if (CropFeverEffectsConfig.hueShiftingShader) {
                val gameRenderer = McClient.self.gameRenderer ?: return
                val accessor = gameRenderer as GameRendererAccessor
                if (gameRenderer.currentPostEffect() != SkyOcean.id(SHADER_ID)) {
                    accessor.invokeSetPostEffect(SkyOcean.id(SHADER_ID))
                }
            }
        }
        if (endRegex.matches(event.text)) {
            turnOff()
        }
    }

    @Subscription(ServerChangeEvent::class)
    fun onServerChange() {
        turnOff()
    }

    @Subscription(ServerDisconnectEvent::class)
    fun onServerDisconnect() {
        turnOff()
    }

    @Subscription
    fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerDev("farming crop_fever") {
            then("start") {
                callback {
                    val startMessageComponent = Text.of("WOAH!") {
                        bold = true
                        color = TextColor.DARK_PURPLE
                        append(" ")
                        append("You caught a case of the") {
                            bold = false
                            color = TextColor.GREEN
                        }
                        append(" ")
                        append("CROP FEVER") {
                            bold = true
                            color = TextColor.PINK
                        }
                        append(" ")
                        append("for 60 seconds!") {
                            bold = false
                            color = TextColor.GREEN
                        }
                    }
                    ChatReceivedEvent.Pre(startMessageComponent).post(SkyBlockAPI.eventBus)
                    startMessageComponent.send()
                }
            }
            then("stop") {
                callback {
                    val endMessageComponent = Text.of("GONE!") {
                        bold = true
                        color = TextColor.DARK_PURPLE
                        append(" ")
                        append("Your") {
                            bold = false
                            color = TextColor.RED
                        }
                        append(" ")
                        append("CROP FEVER") {
                            bold = true
                            color = TextColor.PINK
                        }
                        append(" ")
                        append("has been cured!") {
                            bold = false
                            color = TextColor.RED
                        }
                    }
                    ChatReceivedEvent.Pre(endMessageComponent).post(SkyBlockAPI.eventBus)
                    endMessageComponent.send()
                }
            }
        }
    }
}
