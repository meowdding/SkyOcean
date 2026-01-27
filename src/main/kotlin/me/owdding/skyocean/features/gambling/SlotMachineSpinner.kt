package me.owdding.skyocean.features.gambling

import com.teamresourceful.resourcefullib.common.collections.WeightedCollection
import me.owdding.lib.platform.screens.MeowddingScreen
import me.owdding.skyocean.SkyOcean
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

class SlotMachineSpinner(
    spinPool: Map<SkyBlockId, Int>,
    val winningItem: SkyBlockId?,
    val backgroundTexture: Identifier,
    val slotTexture: Identifier,
) : MeowddingScreen("Slot Machine Spinner") {
    private val armTexture = SkyOcean.id("gambling/arm")
    private val weightedCollection = WeightedCollection.of(spinPool.entries) { it.value.toDouble() }

    private val ticktracker = TickTracker()

    private val slots = mutableListOf<List<SkyBlockId>>()

    private val lastScrollIndex = IntArray(3) { -1 }

    private var startTime = currentInstant() + 0.5.seconds
    private val baseDuration = 3.seconds
    private val waitDelay = 1.seconds
    private val slotHeight = 18

    override fun init() {
        super.init()
        createSlots()
    }

    private fun createSlots() {
        fun random(): SkyBlockId = weightedCollection.next().key

        val final = if (winningItem != null) {
            listOf(winningItem, winningItem, winningItem)
        } else {
            val r1 = random()
            val r2 = random()
            var r3 = random()

            while (r1 == r2 && r2 == r3) {
                r3 = random()
            }
            listOf(r1, r2, r3)
        }

        slots.clear()
        for (i in 0..2) {
            val slotLength = 50 + (i * 20)
            val slot = MutableList(slotLength) { random() }
            slot[slotLength - 2] = final[i]
            slots.add(slot)
        }
    }

    private fun easeOutCubic(x: Float): Float = 1f - (1f - x).pow(3)

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, f: Float) {
        val elapsedTime = startTime.since()
        val scale = 1.5

        for (i in 0..2) {
            graphics.renderSlot(i, scale, elapsedTime)
        }

        graphics.drawSprite(
            backgroundTexture,
            width / 2 - (55 * scale).toInt(),
            height / 2 - (86 * scale).toInt(),
            (110 * scale).toInt(),
            (184 * scale).toInt(),
        )

        val armAnimationIndex = when {
            elapsedTime < 0.seconds -> 0
            elapsedTime < 0.5.seconds -> 1
            else -> 2
        }

        val scaledWidth = (64 * scale).toInt()
        val scaledHeight = (167 * scale).toInt()
        val scaledSpriteHeight = scaledHeight * 3

        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED, armTexture,
            scaledWidth, // spriteWidth
            scaledSpriteHeight, // spriteHeight
            0, // textureX
            scaledHeight * armAnimationIndex, // textureY
            width / 2 + (55 * scale).toInt(), // x
            height / 2 - (scaledHeight / 2) + (38 * scale).toInt(), // y
            scaledWidth, // width
            scaledHeight, // height
        )

        super.render(graphics, mouseX, mouseY, f)
    }

    private fun GuiGraphics.renderSlot(index: Int, scale: Double, elapsedTime: Duration) {
        val spinDuration = baseDuration + (index * waitDelay)

        val rawProgress = (elapsedTime.inWholeMilliseconds / spinDuration.inWholeMilliseconds.toFloat()).coerceIn(0f, 1f)
        val easedProgress = easeOutCubic(rawProgress)

        val xPos = (width / 2) + ((index - 1) * 24 * scale).toInt()
        val yPos = height / 2

        val boxWidth = 40
        val boxHeight = 100
        val boxTop = yPos - (boxHeight / 2)
        val boxLeft = xPos - (boxWidth / 2)

        scissor(boxLeft, boxTop, boxWidth, boxHeight) {
            val slot = slots[index]
            val totalPixelHeight = (slot.size - 2) * slotHeight
            val currentScrollY = totalPixelHeight * easedProgress

            val currentBaseIndex = (currentScrollY / slotHeight).toInt()

            if (elapsedTime < spinDuration && currentBaseIndex > lastScrollIndex[index]) {
                if (lastScrollIndex[index] != -1 && ticktracker.consume()) {
                    val sound = if (ThreadLocalRandom.current().nextInt(0, 1) == 0) {
                        SoundEvents.VAULT_INSERT_ITEM
                    } else {
                        SoundEvents.VAULT_INSERT_ITEM_FAIL
                    }
                    McClient.playSound(sound, 1f, 2f)
                }
                lastScrollIndex[index] = currentBaseIndex
            }

            val pixelOffset = (currentScrollY % slotHeight).toInt()

            drawSprite(
                slotTexture,
                xPos - (9 * scale).toInt(),
                yPos + pixelOffset - (slotHeight * 3 * scale).toInt() + (8 * scale).toInt(),
                (18 * scale).toInt(),
                (90 * scale).toInt(),
            )

            for (offset in -1..2) {
                val targetIndex = (currentBaseIndex + offset).coerceIn(0, slot.size - 1)
                val item = slot[targetIndex]
                val itemY = yPos + pixelOffset - (offset * slotHeight * scale).toInt()
                renderItem(item.toItem(), xPos - 8, itemY - 8)
            }
        }
    }
}

class TickTracker {
    var lastTick = TickEvent.ticks

    fun consume(): Boolean {
        if (lastTick != TickEvent.ticks) {
            lastTick = TickEvent.ticks
            return true
        }
        return false
    }
}
