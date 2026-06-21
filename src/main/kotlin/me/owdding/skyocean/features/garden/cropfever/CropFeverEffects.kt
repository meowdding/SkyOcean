package me.owdding.skyocean.features.garden.cropfever

import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import com.mojang.blaze3d.systems.RenderSystem
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.garden.CropFeverEffectsConfig
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.garden.cropfever.CoinRainOverlay.fallingCoinsList
import me.owdding.skyocean.mixins.GameRendererAccessor
import me.owdding.skyocean.mixins.PostChainAccessor
import me.owdding.skyocean.mixins.PostPassAccessor
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import me.owdding.skyocean.utils.nbs.NBSMusicManager
import net.minecraft.client.renderer.LevelTargetBundle
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import org.lwjgl.system.MemoryStack
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyNonGuest
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.location.ServerDisconnectEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.GARDEN
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.*
import kotlin.time.Instant

@Module
object CropFeverEffects {
    @JvmStatic
    var isFeverActive = false
        private set

    var startTime = Instant.DISTANT_PAST
    private const val LANG_KEY_PATH = CropFeverEffectsConfig.VISUAL_PATH
    private const val RNG_SOUND_ID = "garden.crop_fever.rng"
    private const val BG_MUSIC_SOUND_ID = "garden.crop_fever.music"
    private const val START_SOUND_ID = "garden.crop_fever.start"
    private const val SHADER_ID = "crop_fever_hue_shift"
    const val UNIFORM_ID = "SkyOceanCropFeverHueShift"

    enum class ShiftingSpeedOptions(val speed: Float) : Translatable {
        SLOWER(0.25f),
        SLOW(0.5f),
        NORMAL(1.0f),
        FAST(1.5f),
        FASTER(2.5f),
        INSANE(10.0f);

        override fun getTranslationKey(): String = "$LANG_KEY_PATH.hueShiftingShader.shiftingSpeed.options.${name.lowercase()}"
    }

    enum class CoinRainSpawnMultiplierOptions(val mult: Int) : Translatable {
        AUTO(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        TEN(10),
        FIFTEEN(15),
        TWENTY(20);

        override fun getTranslationKey(): String = "$LANG_KEY_PATH.coinRain.spawnMultiplier.options.${name.lowercase()}"
    }

    val UBO_SIZE = Std140SizeCalculator().putFloat().get()
    private fun updateShaderBuffer() {
        val postChain = McClient.self.shaderManager.getPostChain(SkyOcean.id(SHADER_ID), LevelTargetBundle.MAIN_TARGETS)
        val pass = (postChain as PostChainAccessor).`skyocean$getPasses`().firstOrNull() ?: return
        val buffer = (pass as PostPassAccessor).`skyocean$getCustomUniforms`()[UNIFORM_ID] ?: return

        MemoryStack.stackPush().use { stack ->
            val buf = Std140Builder.onStack(stack, UBO_SIZE)
                .putFloat(CropFeverEffectsConfig.shiftingShaderSpeed.speed)
                .get()
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), buf)
        }
    }

    private fun playSound(id: String, nbsPath: String = "") {
        if (CropFeverEffectsConfig.useRegularSounds) {
            McClient.playSound(SoundEvent(SkyOcean.id(id), Optional.of(1f)), 1f, 1f)
        } else {
            NBSMusicManager.play(id, nbsPath)
        }
    }

    private fun turnOff() {
        if (!isFeverActive) return
        isFeverActive = false

        startTime = Instant.DISTANT_PAST
        if (fallingCoinsList.isNotEmpty()) fallingCoinsList.clear()

        McClient.self.soundManager.stop(SkyOcean.id(RNG_SOUND_ID), SoundSource.PLAYERS)
        McClient.self.soundManager.stop(SkyOcean.id(BG_MUSIC_SOUND_ID), SoundSource.PLAYERS)
        McClient.self.soundManager.stop(SkyOcean.id(START_SOUND_ID), SoundSource.PLAYERS)

        NBSMusicManager.stop(RNG_SOUND_ID)
        NBSMusicManager.stop(BG_MUSIC_SOUND_ID)
        NBSMusicManager.stop(START_SOUND_ID)

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
                playSound(START_SOUND_ID, "garden/crop_fever_start")
            }
            if (CropFeverEffectsConfig.rngSound) {
                playSound(RNG_SOUND_ID, "garden/crop_fever_rng")
            }
            if (CropFeverEffectsConfig.backgroundMusic) {
                playSound(BG_MUSIC_SOUND_ID, "garden/crop_fever_music")
            }
            if (CropFeverEffectsConfig.coinRain) {
                startTime = currentInstant()
            }
            if (CropFeverEffectsConfig.hueShiftingShader) {
                val gameRenderer = McClient.self.gameRenderer ?: return
                val accessor = gameRenderer as GameRendererAccessor
                if (gameRenderer.currentPostEffect() != SkyOcean.id(SHADER_ID)) {
                    updateShaderBuffer()
                    accessor.invokeSetPostEffect(SkyOcean.id(SHADER_ID))
                }
            }
        }
        if (endRegex.matches(event.text)) {
            turnOff()
        }
    }

    @Subscription(ServerChangeEvent::class, ServerDisconnectEvent::class)
    fun onServerChange() {
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
