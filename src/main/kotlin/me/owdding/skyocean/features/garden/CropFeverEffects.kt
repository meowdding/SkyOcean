package me.owdding.skyocean.features.garden

import com.mojang.authlib.GameProfile
import me.owdding.ktmodules.Module
import me.owdding.lib.displays.Displays
import me.owdding.skyocean.config.features.garden.CropFeverEffectsConfig
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.mixins.GameRendererAccessor
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.world.item.ItemStack
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
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.platform.scale
import tech.thatgravyboat.skyblockapi.platform.translate
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import kotlin.time.Instant
import kotlin.time.isDistantPast
import net.minecraft.world.item.Items
import com.mojang.authlib.properties.Property
import net.minecraft.world.item.component.ResolvableProfile
import java.util.UUID
import net.minecraft.core.component.DataComponents
import com.google.common.collect.ImmutableMultimap
import me.owdding.skyocean.utils.nbs.NBSMusicManager

@Module
object CropFeverEffects {
    @JvmStatic
    var isFeverActive = false
        private set

    fun headItemStackFromTexture(texture: String): ItemStack {
        val stack = ItemStack(Items.PLAYER_HEAD)
        val builder = ImmutableMultimap.builder<String, Property>()
        builder.put("textures", Property("textures", texture, ""))
        val propertyMap = com.mojang.authlib.properties.PropertyMap(builder.build())
        val profile = GameProfile(UUID.randomUUID(), "Jhonathan_Smith", propertyMap)
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile))
        return stack
    }

    private val rainItemPool by lazy {
        listOf(
            // Iron Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwMDMwOTQ4MywKICAicHJvZmlsZUlkIiA6ICI1OTgyOWY1ZGY3MmM0ZmFlOTBmOGVhYmM0MjFjMzJkYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQZXBwZXJEcmlua2VyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE2YjkwZjRmYTNlYzEwNmJmZWYyMWYzYjc1ZjU0MWExOGU0NzU3Njc0ZjdkNTgyNTBmYTdlNzQ5NTJmMDg3ZGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
            // Gold Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTcxOTg2ODk5MTUyNCwKICAicHJvZmlsZUlkIiA6ICIxMTM1Njg1ZTk3ZGE0ZjYyYTliNDQ3MzA0NGFiZjQ0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXJpb1dsZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NiY2NlMjJhZjU1OWVkNmJhNjAzODg0NWRiMzhjY2JjYTJlNjJiNzdiODdhMjZhMDY2NTcxMDljZTBlZmJhNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"),
            // Diamond Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTYwMTQ0OTA2NDY5NSwKICAicHJvZmlsZUlkIiA6ICI5ZDEzZjcyMTcxM2E0N2U0OTAwZTMyZGVkNjBjNDY3MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUYWxvZGFvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc0MGQ2ZTM2MmJjN2VlZTRmOTExZGJkMDQ0NjMwN2U3NDU4ZDEwNTBkMDlhZWU1MzhlYmNiMDI3M2NmNzU3NDIiCiAgICB9CiAgfQp9"),
            // Emerald Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTYxMzQwODkzNjg0OCwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lOWQ2MTViZDI3ZGU4ZTU4MGYzOTEzYTFhNDM5ODgwNDI5NTYxN2E2ZjcwYWY1MzJiNDYxMzdkYTVmMGU1ZTJkIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="),
            // Lapis Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTU5OTIxNDA2OTUyMCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWMwZTkxNDQ3NmUxYjE1ZGEyYTkxZjQ1Njk2ZGQyMTc2NjlkNGRhYzRmYTYyMTY1MDkyOWJhY2UwM2RlMjI1NCIKICAgIH0KICB9Cn0="),
        )
    }

    private data class FallingItem(var x: Float, var y: Float, val speed: Float, val item: ItemStack)

    private val fallingItemsList = mutableListOf<FallingItem>()
    private var startTime = Instant.DISTANT_PAST
    private val random = java.util.concurrent.ThreadLocalRandom.current()
    private const val BG_MUSIC_SOUND_ID = "farming.crop_fever.music"
    private const val START_SOUND_ID = "farming.crop_fever.start"

    init {
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.SUBTITLES,
            SkyOcean.id("crop_fever_coins_rain"),
        ) { graphics, _ ->
            if (!startTime.isDistantPast || fallingItemsList.isNotEmpty()) {
                val width = graphics.guiWidth()
                val height = graphics.guiHeight()
                val isWithinSpawnDuration = startTime.since() < CropFeverEffectsConfig.coinsDroppingDuration

                if (isFeverActive && isWithinSpawnDuration && random.nextFloat() < 0.5f) {
                    val randomItem = rainItemPool[random.nextInt(rainItemPool.size)]
                    fallingItemsList.add(
                        FallingItem(
                            x = random.nextFloat() * width,
                            y = -20f,
                            speed = 2f + random.nextFloat() * 5f,
                            item = randomItem,
                        ),
                    )
                }

                val iterator = fallingItemsList.iterator()
                while (iterator.hasNext()) {
                    val coin = iterator.next()
                    coin.y += coin.speed

                    graphics.pushPop {
                        graphics.translate(coin.x, coin.y)
                        graphics.scale(3f, 3f)
                        Displays.item(coin.item).render(graphics, -8, -8)
                    }

                    if (coin.y > height + height * 0.1) iterator.remove() // the height * 0.1 is so that items can be fully offscreen before being removed
                }
            }
        }
    }

    private fun isEnabled() = CropFeverEffectsConfig.enabled && GARDEN.inIsland()

    private fun turnOff() {
        isFeverActive = false

        startTime = Instant.DISTANT_PAST
        fallingItemsList.clear()

        if (NBSMusicManager.isMusicActive(BG_MUSIC_SOUND_ID)) {
            NBSMusicManager.stop(BG_MUSIC_SOUND_ID)
        }
        if (NBSMusicManager.isMusicActive(START_SOUND_ID)) {
            NBSMusicManager.stop(START_SOUND_ID)
        }

        val gameRenderer = McClient.self.gameRenderer
        if (gameRenderer != null) {
            val hueShiftShader = SkyOcean.id("crop_fever_hue_shift")
            if (gameRenderer.currentPostEffect() == hueShiftShader) {
                gameRenderer.clearPostEffect()
            }
        }
    }

    private val group = RemoteStrings.resolve()
    private val cropFeverStartRegex by group.regex("^WOAH! You caught a case of the CROP FEVER for 60 seconds!")
    private val cropFeverEndRegex by group.regex("^GONE! Your CROP FEVER has been cured!")

    @Subscription
    @OnlyIn(GARDEN)
    @OnlyNonGuest
    fun onChatMessage(event: ChatReceivedEvent.Pre) {
        if (!isEnabled()) return
        if (cropFeverStartRegex.matches(event.text)) {
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
                val hueShiftShader = SkyOcean.id("crop_fever_hue_shift")
                if (gameRenderer.currentPostEffect() != hueShiftShader) {
                    accessor.invokeSetPostEffect(hueShiftShader)
                }
            }
        }
        if (cropFeverEndRegex.matches(event.text)) {
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
