package me.owdding.skyocean.features.mining.mineshaft

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MineshaftConfig
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.rendering.RenderUtils.renderTextInWorld
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object CorpseWaypoint {

    private val mineshaftCorpses: List<MineshaftCorpses> = Utils.loadRepoData("mining/shaft_corpses", MineshaftCorpses.CODEC.listOf())

    @Subscription
    @OnlyIn(SkyBlockIsland.MINESHAFT)
    fun onRender(event: RenderWorldEvent.AfterTranslucent) {
        if (!MineshaftConfig.corpseWaypoint) return
        val corpses = mineshaftCorpses.find { it.id == if (MineshaftAPI.isCrystal) "CRYSTAL" else MineshaftAPI.mineshaftType?.id }

        corpses?.positions?.forEach {
            event.renderTextInWorld(Vec3(it), ChatUtils.asSkyOceanColor("Corpse"), true)
        }
    }

    data class MineshaftCorpses(
        val id: String,
        val positions: List<BlockPos>,
    ) {
        companion object {
            val CODEC: Codec<MineshaftCorpses> = RecordCodecBuilder.create { record ->
                record.group(
                    Codec.STRING.fieldOf("id").forGetter(MineshaftCorpses::id),
                    Codec.STRING.xmap(
                        { it.split(",").map { it.toInt() }.let { BlockPos(it[0], it[1], it[2]) } },
                        { "${it.x},${it.y},${it.z}" },
                    ).listOf().fieldOf("locations").forGetter(MineshaftCorpses::positions),
                ).apply(it, ::MineshaftCorpses)
            }
        }
    }
}
