package me.owdding.skyocean.features.gambling.dungeons

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.gambling.GamblingConfig
import me.owdding.skyocean.features.gambling.dungeons.chest.DungeonChestType
import me.owdding.skyocean.features.gambling.dungeons.chest.DungeonItems
import me.owdding.skyocean.utils.rendering.applyPostEffect
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.ARGB
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.platform.*
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import tech.thatgravyboat.skyblockapi.utils.extentions.translated
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Instant
import kotlin.time.isDistantPast

private const val ITEM_SCALE = 4
private const val ITEM_SIZE = 50
private const val WINNER_INDEX = ITEM_SIZE - 10
private const val ITEM_GAP = 5

private const val FULL_CARD_WIDTH = (DungeonCard.WIDTH * ITEM_SCALE) + ITEM_GAP
private const val FULL_CARD_HEIGHT = (DungeonCard.HEIGHT * ITEM_SCALE)

object DungeonGamblingRenderer {

    private val items = mutableListOf<ItemStack>()
    private var randomOffset = 0
    private var start = Instant.DISTANT_PAST
    private var lastSound = 0

    fun init(floor: DungeonFloor, chest: DungeonChestType, winner: ItemStack? = null) {
        start = currentInstant()
        lastSound = 0
        randomOffset = ((4 * ITEM_SCALE) + ThreadLocalRandom.current().nextInt(4 * ITEM_SCALE)) * (if (ThreadLocalRandom.current().nextBoolean()) 1 else -1)
        items.clear()

        val items = DungeonItems[floor, chest] ?: return

        repeat(ITEM_SIZE) {
            this.items.add(items.getRandomItem()?.item ?: ItemStack.EMPTY)
        }
        winner?.let {
            this.items[WINNER_INDEX] = it
        }
    }

    fun cancel() {
        start = Instant.DISTANT_PAST
        items.clear()
    }

    private fun ease(t: Float): Float {
        return if (t < 0.5) (1 - sqrt(1 - (2 * t).pow(2.0f))) / 2f else (sqrt(1 - (-2 * t + 2).pow(2.0f)) + 1) / 2f
    }

    fun extract(graphics: GuiGraphicsExtractor): Boolean {
        if (items.isEmpty()) return false
        if (start.isDistantPast) return false

        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0x80000000.toInt())

        val rawProgress = (start.since() / GamblingConfig.dungeonTime).toFloat()
        val progress = (rawProgress + 0.25f).coerceIn(0f, 1f)
        val endOffset = (WINNER_INDEX * FULL_CARD_WIDTH) * ease(progress)

        if (progress >= 0.96f && rawProgress >= 1f) return false

        val soundIndex = endOffset.toInt() / FULL_CARD_WIDTH

        if (soundIndex > lastSound) {
            McClient.playSound(SoundEvents.ITEM_PICKUP, 1f, 2f)
            lastSound = soundIndex
        }

        graphics.translated(graphics.guiWidth() / 2 - endOffset - ((8 * ITEM_SCALE) + randomOffset), (graphics.guiHeight() - FULL_CARD_HEIGHT) / 2) {

            items.forEachIndexed { index, item ->
                graphics.pushPop {
                    graphics.translate(index * FULL_CARD_WIDTH, 0)
                    graphics.scale(ITEM_SCALE, ITEM_SCALE)

                    DungeonCard.render(
                        graphics,
                        item,
                        ARGB.opaque(item[DataTypes.RARITY]?.color ?: -1),
                    )
                }
            }
        }

        graphics.drawGradient(
            graphics.guiWidth() / 2 - 1, (graphics.guiHeight() + FULL_CARD_HEIGHT) / 2,
            2, (DungeonCard.HEIGHT * ITEM_SCALE * 0.25).toInt(),
            0xFFFF5555.toInt(), 0xFFFF5555.toInt(), 0xFFFF5555.toInt(), 0xFFFF5555.toInt(),
        )

        graphics.applyPostEffect(SkyOcean.id("case_screen"))

        if (progress >= 0.96f) {
            val winnerItem = items[WINNER_INDEX]
            val winner = winnerItem.getSkyBlockId()?.toItem()?.hoverName.takeIf { winnerItem.getSkyBlockId()?.isEnchantment == true } ?: winnerItem.hoverName
            val length = McFont.width(winner)

            val scale = Mth.lerp((progress - 0.96f) / 0.04f, 1f, 3f)

            graphics.translated(graphics.guiWidth() / 2, graphics.guiHeight() * 0.2f) {
                graphics.scale(scale, scale)
                graphics.translate(-length / 2f, 0f)
                graphics.drawString(winner, 0, 0)
            }
        }

        return true
    }
}
