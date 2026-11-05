package me.owdding.skyocean.features.misc.`fun`

import me.owdding.ktmodules.Module
import me.owdding.lib.displays.Displays
import me.owdding.lib.overlays.Position
import me.owdding.lib.overlays.Rect
import me.owdding.skyocean.config.hidden.OverlayPositions
import me.owdding.skyocean.features.misc.`fun`.animal.modifiers.CatModifier
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import me.owdding.skyocean.utils.TickTracker
import me.owdding.skyocean.utils.Utils.get
import me.owdding.skyocean.utils.rendering.Overlay
import me.owdding.skyocean.utils.rendering.SkyOceanOverlay
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.animal.feline.Cat
import net.minecraft.world.entity.animal.feline.CatSoundVariants
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.platform.translate
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.regex.component.match
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.time.Instant

@Module
@Overlay
object MaMowOverlay : SkyOceanOverlay() {

    private var isEnabled = false

    val mouseRange = -50..50

    private val sunny by RemoteStrings.resolve().componentRegex("(?<prefix>\\[NPC] Sunny: ).*")

    override val name: Component = Text.of("MaMow")
    override val position: Position = OverlayPositions.mamow
    override val bounds: Pair<Int, Int> get() = McClient.window.guiScaledWidth to McClient.window.guiScaledHeight
    override val editBounds: Rect get() = Rect(0, 0, 0, 0)
    override val enabled: Boolean get() = isEnabled

    data class FallingCat(
        val x: Float,
        val spawnTime: Instant,
        val speed: Float,
        val distribution: Int,
        val cat: Cat,
        val mouseX: Float,
        val mouseY: Float,
    )

    private val fallingCatsList = mutableListOf<FallingCat>()
    private val random = ThreadLocalRandom.current()
    private val spawnTracker = TickTracker()
    private val soundsTracker = TickTracker()

    override fun extract(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val now = currentInstant()
        val width = McClient.window.guiScaledWidth
        val height = McClient.window.guiScaledHeight
        val minFallSpeed = height * 0.9f
        val maxFallSpeed = height * 1.1f

        val iterator = fallingCatsList.iterator()
        while (iterator.hasNext()) {
            val cat = iterator.next()
            val y = cat.speed * ((now - cat.spawnTime).inWholeMilliseconds / 1000f) - cat.distribution

            if (y > height * 1.2f) {
                iterator.remove()
                continue
            }

            graphics.pushPop {
                graphics.translate(cat.x, y)
                Displays.entity(
                    cat.cat,
                    100,
                    100,
                    100,
                    50 + cat.mouseX,
                    50 + cat.mouseY,
                ).extract(graphics, -8, -(height * 0.05).toInt())
            }
        }

        val referenceArea = 480f * 270f //1920x1080 at GUI Scale 4
        val scaledArea = width * height
        val catMultiplier = (scaledArea / referenceArea).coerceAtLeast(1f).toInt()

        if (spawnTracker.consume() && Random.nextDouble() < 0.2) {
            repeat(catMultiplier) { // this might cause a performance issues at high resolutions, im not sure
                fallingCatsList.add(
                    FallingCat(
                        x = random.nextFloat() * width,
                        spawnTime = now,
                        speed = minFallSpeed + random.nextFloat() * (maxFallSpeed - minFallSpeed),
                        cat = createRandomCat() ?: return@repeat,
                        distribution = (0..500).random(),
                        mouseX = mouseRange.random().toFloat(),
                        mouseY = mouseRange.random().toFloat(),
                    ),
                )
            }
        }

        if (soundsTracker.consume() && Random.nextDouble() < 0.2) {
            val cat = fallingCatsList.randomOrNull()?.cat ?: return
            val sound = cat.soundSet.let {
                listOf(it.ambientSound, it.begForFoodSound, it.eatSound, it.purrSound, it.purreowSound, it.strayAmbientSound)
            }.random()
            McClient.playSound(sound.value())
        }
    }

    private fun createRandomCat(): Cat? {
        val level = McLevel.selfOrNull ?: return null
        val cat = Cat(EntityTypes.CAT, level)
        cat.setSoundVariant(CatSoundVariants.pickRandomSoundVariant(cat.registryAccess(), level.getRandom()))
        CatModifier.Variant.entries.random().catVariant?.let {
            cat.variant = Registries.CAT_VARIANT.get(it)
        }
        cat.isBaby = (0..10).random() < 2
        return cat
    }

    @Subscription
    private fun onCommand(event: RegisterCommandsEvent) {
        event.registerWithCallback("mamow") {
            isEnabled = !isEnabled
        }
    }

    @Subscription
    private fun onChat(event: ChatReceivedEvent.Post) {
        if ((0..10).random() != 0) return
        sunny.match(event.component, "prefix") { (prefix) ->
            event.component = Text.join(prefix, "run /mamow")
        }
    }
}
