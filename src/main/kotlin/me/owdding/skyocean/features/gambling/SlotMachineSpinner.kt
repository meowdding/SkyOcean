package me.owdding.skyocean.features.gambling

import com.teamresourceful.resourcefullib.common.collections.WeightedCollection
import me.owdding.lib.platform.screens.MeowddingScreen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.drawFilledBox
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

class SlotMachineSpinner(spinPool: Map<SkyBlockId, Int>, val winningItem: SkyBlockId?) : MeowddingScreen("Slot Machine Spinner") {
    private val weightedCollection = WeightedCollection.of(spinPool.entries) { it.value.toDouble() }

    private val slots = mutableListOf<List<SkyBlockId>>()

    private val lastScrollIndex = IntArray(3) { -1 }

    private var startTime = currentInstant()
    private val baseDuration = 3.seconds
    private val waitDelay = 1.seconds
    private val slotHeight = 40

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

        for (i in 0..2) {
            val spinDuration = baseDuration + (i * waitDelay)

            val rawProgress = (elapsedTime.inWholeMilliseconds / spinDuration.inWholeMilliseconds.toFloat()).coerceIn(0f, 1f)
            val easedProgress = easeOutCubic(rawProgress)

            val xPos = (width / 2) + ((i - 1) * 70)
            val yPos = height / 2

            val boxWidth = 40
            val boxHeight = 100
            val boxTop = yPos - (boxHeight / 2)
            val boxLeft = xPos - (boxWidth / 2)

            graphics.drawFilledBox(boxLeft, boxTop, boxWidth, boxHeight, 0xA0000000.toInt())

            graphics.scissor(boxLeft, boxTop, boxWidth, boxHeight) {
                val slot = slots[i]
                val totalPixelHeight = (slot.size - 2) * slotHeight
                val currentScrollY = totalPixelHeight * easedProgress

                val currentBaseIndex = (currentScrollY / slotHeight).toInt()

                if (elapsedTime < spinDuration && currentBaseIndex > lastScrollIndex[i]) {
                    if (lastScrollIndex[i] != -1) {
                        McClient.playSound(SoundEvents.ITEM_PICKUP, 1f, 2f)
                    }
                    lastScrollIndex[i] = currentBaseIndex
                }

                val pixelOffset = (currentScrollY % slotHeight).toInt()

                for (offset in -1..2) {
                    val targetIndex = (currentBaseIndex + offset).coerceIn(0, slot.size - 1)
                    val item = slot[targetIndex]
                    val itemY = yPos + pixelOffset - (offset * slotHeight)
                    graphics.renderItem(item.toItem(), xPos - 8, itemY - 8)
                }
            }
        }

        super.render(graphics, mouseX, mouseY, f)
    }
}
