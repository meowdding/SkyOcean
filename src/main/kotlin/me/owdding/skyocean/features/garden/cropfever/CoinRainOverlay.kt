package me.owdding.skyocean.features.garden.cropfever

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import me.owdding.lib.displays.Displays
import me.owdding.lib.overlays.Position
import me.owdding.skyocean.config.features.garden.CropFeverEffectsConfig
import me.owdding.skyocean.config.hidden.OverlayPositions
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.rendering.Overlay
import me.owdding.skyocean.utils.rendering.SkyOceanOverlay
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.platform.scale
import tech.thatgravyboat.skyblockapi.platform.translate
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import java.util.UUID
import kotlin.time.Instant
import me.owdding.lib.overlays.Rect
import net.minecraft.client.gui.GuiGraphicsExtractor

@Overlay
object CoinRainOverlay : SkyOceanOverlay() {

    override val name: Component = +"overlays.crop_fever_effects.coin_rain"
    override val position: Position = OverlayPositions.cropFeverCoinRain
    override val bounds: Pair<Int, Int> get() = McClient.window.guiScaledWidth to McClient.window.guiScaledHeight
    override val editBounds: Rect get() = Rect(0, 0, 0, 0)
    override val enabled: Boolean get() = CropFeverEffects.isFeverActive

    data class FallingCoin(val x: Float, val spawnTime: Instant, val speed: Float, val item: ItemStack)

    val fallingCoinsList = mutableListOf<FallingCoin>()
    private val random = java.util.concurrent.ThreadLocalRandom.current()
    private const val COIN_SPAWN_INTERVAL_MS = 40

    fun headItemStackFromTexture(texture: String): ItemStack { // this should maybe be moved somewhere else?
        val stack = ItemStack(Items.PLAYER_HEAD)
        val builder = ImmutableMultimap.builder<String, Property>()
        builder.put("textures", Property("textures", texture, ""))
        val propertyMap = PropertyMap(builder.build())
        val profile = GameProfile(UUID.randomUUID(), "Jhonathan_Smith", propertyMap)
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile))
        return stack
    }

    private val coinRainItems by lazy {
        listOf(
            // Iron Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwMDMwOTQ4MywKICAicHJvZmlsZUlkIiA6ICI1OTgyOWY1ZGY3MmM0ZmFlOTBmOGVhYmM0MjFjMzJkYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQZXBwZXJEcmlua2VyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE2YjkwZjRmYTNlYzEwNmJmZWYyMWYzYjc1ZjU0MWExOGU0NzU3Njc0ZjdkNTgyNTBmYTdlNzQ5NTJmMDg3ZGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
            // Gold Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTcxOTg2ODk5MTUyNCwKICAicHJvZmlsZUlkIiA6ICIxMTM1Njg1ZTk3ZGE0ZjYyYTliNDQ3MzA0NGFiZjQ0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXJpb1dsZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NiY2NlMjJhZjU1OWVkNmJhNjAzODg0NWRiMzhjY2JjYTJlNjJiNzdiODdhMjZhMDY2NTcxMDljZTBlZmJhNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"),
            // Diamond Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTYwMTQ0OTA2NDY5NSwKICAicHJvZmlsZUlkIiA6ICI5ZDEzZjcyMTcxM2E0N2U0OTAwZTMyZGVkNjBjNDY3MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUYWxvZGFvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc0MGQ2ZTM2MmJjN2VlZTRmOTExZGJkMDQ0NjMwN2U3NDU4ZDEwNTBkMDlhZWU1MzhlYmNiMDI3M2NmNzU3NDIiCiAgICB9CiAgfQp9"),
            // Emerald Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTYxMzQwODkzNjg0OCwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lOWQ2MTViZDI3ZGU4ZTU4MGYzOTEzYTFhNDM5ODgwNDI5NTYxN2E2ZjcwYWY1MzJiNDYxMzdkYTVmMGU1ZTJkIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="),
            // Redstone Coin - https://namemc.com/skin/157e4ebb7160dbb6
            headItemStackFromTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjNGY2NjMwNTk3YTQ5YWQyMjNkMTJjZjY0OGFmMjI4M2QzNGE2NWJmOWRkMDU3ZDE5OGQyOTgwNzc5YzM0In19fQ=="),
            // Lapis Coin
            headItemStackFromTexture("ewogICJ0aW1lc3RhbXAiIDogMTU5OTIxNDA2OTUyMCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWMwZTkxNDQ3NmUxYjE1ZGEyYTkxZjQ1Njk2ZGQyMTc2NjlkNGRhYzRmYTYyMTY1MDkyOWJhY2UwM2RlMjI1NCIKICAgIH0KICB9Cn0="),
        )
    }

    override fun extract(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val now = currentInstant()
        val width = McClient.window.guiScaledWidth
        val height = McClient.window.guiScaledHeight

        val iterator = fallingCoinsList.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            val y = coin.speed * ((now - coin.spawnTime).inWholeMilliseconds / 1000f)

            if (y > height * 1.2f) {
                iterator.remove()
                continue
            }

            graphics.pushPop {
                graphics.translate(coin.x, y)
                graphics.scale(3, 3)
                Displays.item(coin.item).extract(graphics, -8, -(height * 0.05).toInt())
            }
        }

        val shouldCoinsKeepSpawning = (now - CropFeverEffects.startTime) < CropFeverEffectsConfig.coinsDroppingDuration
        val timeSinceLastCoin = (now - (fallingCoinsList.lastOrNull()?.spawnTime ?: Instant.DISTANT_PAST)).inWholeMilliseconds

        if (shouldCoinsKeepSpawning && timeSinceLastCoin >= COIN_SPAWN_INTERVAL_MS) {

            val minFallSpeed = height * 0.9f
            val maxFallSpeed = height * 1.1f

            fallingCoinsList.add(
                FallingCoin(
                    x = random.nextFloat() * width,
                    spawnTime = now,
                    speed = minFallSpeed + random.nextFloat() * (maxFallSpeed - minFallSpeed),
                    item = coinRainItems.random(),
                ),
            )
        }
    }
}
