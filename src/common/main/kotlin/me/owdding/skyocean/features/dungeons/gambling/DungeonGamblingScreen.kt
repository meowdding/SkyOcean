package me.owdding.skyocean.features.dungeons.gambling

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.api.SkyOceanItemId.Companion.getSkyOceanId
import me.owdding.skyocean.features.dungeons.gambling.chest.DungeonChestType
import me.owdding.skyocean.features.dungeons.gambling.chest.DungeonItems
import me.owdding.skyocean.utils.SkyOceanPopupScreen
import me.owdding.skyocean.utils.rendering.applyPostEffect
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.ARGB
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.platform.*
import tech.thatgravyboat.skyblockapi.utils.extentions.get
import tech.thatgravyboat.skyblockapi.utils.extentions.translated
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow
import kotlin.math.sqrt

private const val ITEM_SCALE = 4
private const val ITEM_SIZE = 50
private const val WINNER_INDEX = ITEM_SIZE - 10
private const val ITEM_GAP = 5
private const val TIME = 5 * 1000f

private const val FULL_CARD_WIDTH = (DungeonCard.WIDTH * ITEM_SCALE) + ITEM_GAP
private const val FULL_CARD_HEIGHT = (DungeonCard.HEIGHT * ITEM_SCALE)

class DungeonGamblingScreen(
    val floor: DungeonFloor,
    val chest: DungeonChestType,
    val winner: ItemStack? = null,
    parentScreen: Screen? = null,
) : SkyOceanPopupScreen(parentScreen) {

    private var items = mutableListOf<ItemStack>()
    private var randomOffset = 0
    private var start = 0L
    private var lastSound = 0

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        super.init()
        start()
    }

    fun start() {
        start = System.currentTimeMillis()
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

    fun ease(t: Float): Float {
        return if (t < 0.5) (1 - sqrt(1 - (2 * t).pow(2.0f))) / 2f else (sqrt(1 - (-2 * t + 2).pow(2.0f)) + 1) / 2f
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.render(graphics, mouseX, mouseY, partialTicks)

        val rawProgress = (((System.currentTimeMillis() - start) / TIME))
        val progress = (rawProgress + 0.25f).coerceIn(0f, 1f)
        val endOffset = (WINNER_INDEX * FULL_CARD_WIDTH) * ease(progress)

        val soundIndex = endOffset.toInt() / FULL_CARD_WIDTH

        if (soundIndex > lastSound) {
            McClient.playSound(SoundEvents.ITEM_PICKUP, 1f, 2f)
            lastSound = soundIndex
        }

        graphics.translated(this.width / 2 - endOffset - ((8 * ITEM_SCALE) + randomOffset), (this.height - FULL_CARD_HEIGHT) / 2) {

            items.forEachIndexed { index, item ->
                graphics.pushPop {
                    graphics.translate(index * FULL_CARD_WIDTH, 0)
                    graphics.scale(ITEM_SCALE, ITEM_SCALE)

                    DungeonCard(
                        item,
                        ARGB.opaque(item[DataTypes.RARITY]?.color ?: -1),
                    ).render(graphics, mouseX, mouseY, partialTicks)
                }
            }
        }

        graphics.drawGradient(
            this.width / 2 - 1, (this.height + FULL_CARD_HEIGHT) / 2,
            2, (DungeonCard.HEIGHT * ITEM_SCALE * 0.25).toInt(),
            0xFFFF5555.toInt(), 0xFFFF5555.toInt(), 0xFFFF5555.toInt(), 0xFFFF5555.toInt(),
        )

        graphics.applyPostEffect(SkyOcean.id("case_screen"))

        if (progress >= 0.96f && items.isNotEmpty()) {
            val winnerItem = items[WINNER_INDEX]
            val winner = winnerItem.getSkyOceanId()?.toItem()?.hoverName.takeIf { winnerItem.getSkyOceanId()?.isEnchantment == true } ?: winnerItem.hoverName
            val length = McFont.width(winner)

            val scale = Mth.lerp((progress - 0.96f) / 0.04f, 1f, 3f)

            // todo: better closing (closes too fast (basically instantly))
            if (rawProgress >= 1f) {
                McClient.runNextTick { onClose() }
            }

            graphics.translated(this.width / 2, this.height * 0.2f) {
                graphics.scale(scale, scale)
                graphics.translate(-length / 2f, 0f)
                graphics.drawString(winner, 0, 0)
            }
        }
    }
}
