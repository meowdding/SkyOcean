package me.owdding.skyocean.features.foraging.galatea

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.foraging.GalateaConfig
import me.owdding.skyocean.repo.misc.GalateaRepoData
import net.minecraft.core.BlockPos
import net.minecraft.util.ARGB
import net.minecraft.world.level.block.entity.BeaconBeamOwner
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyWidget
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.info.TabWidgetChangeEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.anyMatch
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@Module
object MoongladeBeacon {

    private var beaconActive: Boolean = false

    private val inactiveRegex = " Cooldown: AVAILABLE".toRegex()

    private val redSection = listOf(BeaconBeamOwner.Section(ARGB.color(32, TextColor.RED)))
    private val greenSection = listOf(BeaconBeamOwner.Section(ARGB.color(32, TextColor.GREEN)))

    private val beaconPos: BlockPos get() = GalateaRepoData.data?.moongladeBeaconPos ?: BlockPos(-688, 128, 65)

    @Subscription
    @OnlyWidget(MOONGLADE_BEACON)
    @OnlyIn(GALATEA)
    fun onWidget(event: TabWidgetChangeEvent) {
        beaconActive = !inactiveRegex.anyMatch(event.new)
    }

    @Subscription(ServerChangeEvent::class)
    fun onServerChange() {
        beaconActive = false
    }

    @JvmStatic
    fun isBlockPos(pos: BlockPos): Boolean = SkyBlockIsland.GALATEA.inIsland() && pos == beaconPos && GalateaConfig.moongladeBeaconColor

    @JvmStatic
    fun getSection(): List<BeaconBeamOwner.Section> = if (beaconActive) greenSection else redSection

}
