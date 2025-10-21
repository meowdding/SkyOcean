package me.owdding.skyocean.features.mining.mineshaft

import com.mojang.serialization.Codec
import me.owdding.ktmodules.Module
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.mining.MineshaftConfig
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.extensions.putAll
import me.owdding.skyocean.utils.rendering.RenderUtils.renderTextInWorld
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftAPI
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftType
import tech.thatgravyboat.skyblockapi.api.area.mining.mineshaft.MineshaftVariant
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.level.RightClickEntityEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.codecs.CodecUtils
import tech.thatgravyboat.skyblockapi.utils.extentions.filterKeysNotNull
import tech.thatgravyboat.skyblockapi.utils.extentions.getArmor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.url

@Module
object CorpseWaypoint {

    private val CODEC = CodecUtils.map(
        Codec.STRING.xmap({ MineshaftType.fromId(it) }, { it?.id }),
        CodecUtils.map(
            Codec.STRING.xmap({ v -> MineshaftVariant.entries.find { it.name == v } }, { it?.name }),
            CodecHelpers.BLOCK_POS_STRING_CODEC.listOf(),
        ),
    )


    private val mineshaftCorpses: MutableMap<MineshaftType, Map<MineshaftVariant, List<BlockPos>>> = mutableMapOf()

    @Subscription(FinishRepoLoadingEvent::class)
    fun onRepoLoad() {
        mineshaftCorpses.putAll(
            Utils.loadRemoteRepoData("mining/mineshaft_corpses", CODEC)
                ?.filterKeysNotNull()
                ?.map { (k, v) -> k to v.filterKeysNotNull() }
                ?.toMap(),
        )
    }

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

    @Subscription
    @OnlyIn(SkyBlockIsland.MINESHAFT)
    fun onInteract(event: RightClickEntityEvent) {
        if (!MineshaftConfig.corpseWaypoint) return
        val entity = event.entity as? ArmorStand ?: return

        if (entity.isInvisible) return
        if (entity.getArmor().any { it.isEmpty }) return

        val mineshaft = mineshaftCorpses.entries.find { it.key == MineshaftAPI.mineshaftType } ?: return
        val corpses = mineshaft.value.entries.find { it.key == MineshaftAPI.mineshaftVariant }?.value ?: return

        val isKnown = corpses.any { corpsePos -> corpsePos.distSqr(entity.blockPosition()) < 4.0 }
        if (isKnown) return

        Text.of {
            append("Unknown corpse location for ${MineshaftAPI.mineshaftType?.name}-${MineshaftAPI.mineshaftVariant?.name} found! ")
            append("If you want to help us continue supporting this feature, please join our ")
            append("Discord") {
                underlined = true
                color = TextColor.BLUE
                url = SkyOcean.DISCORD
            }
            append(" and send a screenshot of this message into the #support channel.")
            append(" (${entity.blockPosition()})")
        }.sendWithPrefix()
    }
}
