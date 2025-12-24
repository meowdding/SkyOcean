package me.owdding.skyocean.features.garden

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.garden.GardenConfig
import me.owdding.skyocean.utils.SoundUtils
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyNonGuest
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.LeftClickBlockEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.GARDEN
import tech.thatgravyboat.skyblockapi.api.profile.garden.PlotAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import tech.thatgravyboat.skyblockapi.utils.time.since
import kotlin.time.Instant

@Module
object PestWarning {

    private const val PEST_AMOUNT = 4
    private var lastWarning = Instant.DISTANT_PAST

    // TODO: Maybe move to Crop enum in sbapi
    private val cropBlocks = setOf(
        Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM, Blocks.CARROTS, Blocks.POTATOES, Blocks.WHEAT,
        Blocks.COCOA, Blocks.CARVED_PUMPKIN, Blocks.PUMPKIN, Blocks.MELON, Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.NETHER_WART,
    )

    @Subscription
    @OnlyIn(GARDEN)
    @OnlyNonGuest
    fun onBlockClick(event: LeftClickBlockEvent) {
        if (McLevel[event.pos].block !in cropBlocks) return
        val pests = PlotAPI.currentPestAmount
        if (pests < PEST_AMOUNT) return
        if (GardenConfig.pestWarning && lastWarning.since() > GardenConfig.pestWarningDelay) {
            lastWarning = currentInstant()
            val title = Text.of {
                append(ChatUtils.ICON_SPACE_COMPONENT)
                append("$pests pests!") {
                    color = OceanColors.WARNING
                    bold = true
                }
            }
            McClient.setTitle(title, null, 0f, 3f, 0.5f)
            SoundUtils.playRepeated(SoundEvents.NOTE_BLOCK_PLING.value(), 10, 4F, 0.5F)

            Text.of {
                append("There are ") { color = OceanColors.WARNING }
                append("$pests ") { color = OceanColors.BETTER_GOLD }
                append("pests! Kill them to get your Farming Fortune back") { color = OceanColors.WARNING }
            }.sendWithPrefix()
        }
    }

    @Subscription(ServerChangeEvent::class)
    fun onWorldChange() {
        lastWarning = Instant.DISTANT_PAST
    }

}
