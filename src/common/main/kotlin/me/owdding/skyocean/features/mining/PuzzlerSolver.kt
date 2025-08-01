package me.owdding.skyocean.features.mining

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.utils.Utils.plus
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.ModelBlockRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.StairBlock
import org.joml.Vector3d
import org.joml.plus
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel

@Module
object PuzzlerSolver {

    val directions = mapOf(
        '▲' to Vector3d(0.0, 0.0, 1.0),
        '◀' to Vector3d(1.0, 0.0, 0.0),
        '▶' to Vector3d(-1.0, 0.0, 0.0),
        '▼' to Vector3d(0.0, 0.0, -1.0),
    )

    private val regex = Regex("\\[NPC] Puzzler: [▲◀▶▼]+")

    private val location: BlockPos = BlockPos(181, 195, 135)
    private var solution: BlockPos? = null

    @Subscription(event = [ServerChangeEvent::class])
    fun reset() {
        solution = null
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.DWARVEN_MINES)
    fun onRenderWorld(event: RenderWorldEvent.AfterTranslucent) {
        if (!MiningConfig.puzzlerSolver) return
        val solution = solution ?: return
        event.atCamera {
            translate(solution.x.toFloat(), solution.y.toFloat(), solution.z.toFloat())

            val worldBlock = McLevel.self.getBlockState(solution)

            val state = if (worldBlock.block is StairBlock) {
                Blocks.WARPED_STAIRS.withPropertiesOf(worldBlock)
            } else {
                Blocks.WARPED_PLANKS.defaultBlockState()
            }

            ModelBlockRenderer.renderModel(
                event.poseStack.last(),
                event.buffer.getBuffer(RenderType.entityCutoutNoCullZOffset(TextureAtlas.LOCATION_BLOCKS)),
                McClient.self.blockRenderer.getBlockModel(state),
                1f,
                1f,
                1f,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
            )
        }
    }

    @Subscription(receiveCancelled = true)
    @OnlyIn(SkyBlockIsland.DWARVEN_MINES)
    fun onChat(event: ChatReceivedEvent.Pre) {
        if (!event.text.startsWith("[NPC] Puzzler: ")) return
        if (!regex.matches(event.text)) return reset()

        val vec = event.text.removePrefix("[NPC] Puzzler: ")
            .trim()
            .mapNotNull { directions.getOrDefault(it, null) }
            .reduce { acc, i -> acc + i }

        solution = location + vec
    }
}
