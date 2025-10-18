package me.owdding.skyocean.features.mining.mineshaft

import com.mojang.serialization.Codec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MineshaftConfig
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.rendering.RenderUtils.renderTextInWorld
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftAPI
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftType
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftVariant
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.codecs.CodecUtils
import tech.thatgravyboat.skyblockapi.utils.extentions.filterKeysNotNull

@Module
object CorpseWaypoint {

    private val CODEC = CodecUtils.map(
        Codec.STRING.xmap({ MineshaftType.fromId(it) }, { it?.id }),
        CodecUtils.map(
            Codec.STRING.xmap({ v -> MineshaftVariant.entries.find { it.name == v } }, { it?.name }),
            CodecHelpers.BLOCK_POS_STRING_CODEC.listOf(),
        ),
    )

    private val mineshaftCorpses: Map<MineshaftType, Map<MineshaftVariant, List<BlockPos>>> =
        Utils.loadRepoData("mining/shaft_corpses", CODEC).filterKeysNotNull().map { (k, v) -> k to v.filterKeysNotNull() }.toMap()

    @Subscription
    @OnlyIn(SkyBlockIsland.MINESHAFT)
    fun onRender(event: RenderWorldEvent.AfterTranslucent) {
        if (!MineshaftConfig.corpseWaypoint) return
        val mineshaft = mineshaftCorpses.entries.find { it.key == MineshaftAPI.mineshaftType } ?: return
        val corpses = mineshaft.value.entries.find { it.key == MineshaftAPI.mineshaftVariant }?.value ?: return

        corpses.forEach {
            event.renderTextInWorld(Vec3(it), ChatUtils.asSkyOceanColor("Corpse"), true)
        }
    }
}
