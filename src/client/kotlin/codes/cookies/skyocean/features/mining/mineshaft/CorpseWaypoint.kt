package codes.cookies.skyocean.features.mining.mineshaft

import codes.cookies.skyocean.config.features.mining.MineshaftConfig
import codes.cookies.skyocean.events.RenderWorldEvent
import codes.cookies.skyocean.modules.Module
import codes.cookies.skyocean.utils.ChatUtils
import codes.cookies.skyocean.utils.RenderUtils.renderTextInWorld
import codes.cookies.skyocean.utils.Utils
import com.google.gson.JsonArray
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland

@Module
object CorpseWaypoint {

    private var mineshaftCorpses: List<MineshaftCorpses> = emptyList()

    private val CODEC = MineshaftCorpses.CODEC.listOf()

    data class MineshaftCorpses(
        val id: String,
        val positions: List<Vector3f>,
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.create {
                it.group(
                    Codec.STRING.fieldOf("id").forGetter(MineshaftCorpses::id),
                    ExtraCodecs.VECTOR3F.listOf().fieldOf("locations").forGetter(MineshaftCorpses::positions),
                ).apply(it, ::MineshaftCorpses)
            }
        }
    }

    init {
        Utils.loadFromRepo<JsonArray>("mining/shaft_corpses").let {
            CODEC.parse(JsonOps.INSTANCE, it ?: JsonArray()).let {
                if (it.isError) {
                    throw RuntimeException(it.error().get().message())
                }
                mineshaftCorpses = it.getOrThrow()
            }
        }
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.MINESHAFT)
    fun onRender(event: RenderWorldEvent) {
        if (!MineshaftConfig.corpseWaypoint) return
        val corpses = mineshaftCorpses.find { it.id == if (MineshaftAPI.isCrystal) "CRYSTAL" else MineshaftAPI.mineshaftType?.id }

        corpses?.positions?.forEach {
            event.renderTextInWorld(Vec3(it), ChatUtils.asSkyOceanColor("Corpse"), true)
        }
    }
}
