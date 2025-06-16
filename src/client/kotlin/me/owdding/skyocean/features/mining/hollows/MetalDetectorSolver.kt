package me.owdding.skyocean.features.mining.hollows

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.ChatUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.chat.ActionBarReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.NameChangedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockAreas
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

@Suppress("unused")
@Module
object MetalDetectorSolver {

    val treasureRegex = Regex("""TREASURE: (\d+\.\d+)m""")

    val keeperOffset = mapOf(
        "Gold" to Vec3i(3, 0, -33),
        "Diamond" to Vec3i(33, 0, 3),
        "Emerald" to Vec3i(-3, 0, 33),
        "Lapis" to Vec3i(-3, 0, 33),
    )

    val locationsRaw = arrayOf(
        Vec3i(-24, -22, 12),
        Vec3i(-42, -20, -28),
        Vec3i(30, -21, -25),
        Vec3i(40, -22, -30),
        Vec3i(-14, -21, 43),
        Vec3i(-23, -22, 40),
        Vec3i(23, -22, -39),
        Vec3i(42, -19, -41),
        Vec3i(6, -21, 28),
        Vec3i(-43, -22, -40),
        Vec3i(-12, -21, -44),
        Vec3i(12, -21, -43),
        Vec3i(25, -22, 17),
        Vec3i(20, -21, -26),
        Vec3i(-37, -21, -14),
        Vec3i(1, -21, 20),
        Vec3i(-5, -21, 16),
        Vec3i(22, -21, -14),
        Vec3i(7, -21, 22),
        Vec3i(-40, -22, 18),
        Vec3i(12, -21, 7),
        Vec3i(-1, -22, -20),
        Vec3i(38, -22, -26),
        Vec3i(12, -22, 31),
        Vec3i(-20, -22, 0),
        Vec3i(43, -21, -16),
        Vec3i(-17, -21, 20),
        Vec3i(-31, -21, -40),
        Vec3i(29, -21, -44),
        Vec3i(20, -22, 0),
        Vec3i(-14, -21, 22),
        Vec3i(12, -22, -22),
        Vec3i(-37, -21, -22),
        Vec3i(7, -21, 11),
        Vec3i(-36, -20, 42),
        Vec3i(19, -22, 29),
        Vec3i(-38, -22, 26),
        Vec3i(-31, -21, -12),
        Vec3i(24, -22, 27)
    )

    val locations = arrayListOf<BlockPos>()

    var distance: Double? = null
    var center: BlockPos? = null

    var oldPos: Vec3 = Vec3.ZERO

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    @TimePassed("1s")
    fun eachSecond(event: TickEvent) {
        val currentPos = McPlayer.position?: return
        if (!searchCheck()) return
        if (center != null) {
            if (oldPos == currentPos) {
                check(currentPos)
            }
        } else {
            ChatUtils.chat("Move close to a keeper to get proper locations")
        }
        oldPos = currentPos
    }

    private fun check(pos: Vec3) {
        SkyOcean.info("Checking ${pos.x}, ${pos.y}, ${pos.z} with distance of ${distance ?: 0.0}")
        locations.forEach { loc ->
            val dist = distance ?: return@forEach

            val distToLower = loc.distToLowCornerSqr(pos.x, pos.y, pos.z)
            if (distToLower in dist-4.0..dist+4.0) {
                SkyOcean.info("Found chest at {} {} {} with distance {}", loc.x, loc.y, loc.z, distToLower)
            }
        }
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onActionBar(event: ActionBarReceivedEvent.Pre) {
        if (searchCheck()) {
            val split = event.text.split("     ")
            for (widget in split) {
                if (widget.matches(treasureRegex)) {
                    treasureRegex.matchEntire(widget)?.let {
                        distance = it.groupValues[1].toDouble() + 1
                    }
                }
            }
        }
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onEntityNamed(event: NameChangedEvent) {
        if (event.infoLineEntity is ArmorStand && event.literalComponent.startsWith("Keeper of ")) {
            val type = event.literalComponent.substringAfter("Keeper of ")
            val blockPos = event.infoLineEntity.blockPosition()
            val offset = keeperOffset[type]?: return
            if (center == null) {
                val newPos = BlockPos(blockPos.x + offset.x, blockPos.y + offset.y, blockPos.z + offset.z)
                center = newPos
                locations.ifEmpty {
                    locationsRaw.forEach { location ->
                        locations.add(BlockPos(location.x + newPos.x, location.y + newPos.y, location.z + newPos.z))
                    }
                }
            }
        }
    }

    @Subscription
    fun onServerChange(event: ServerChangeEvent) {
        distance = null
        center = null
        oldPos = Vec3.ZERO
        locations.clear()
    }

    fun searchCheck(): Boolean = LocationAPI.area == SkyBlockAreas.MINES_OF_DIVAN && McPlayer.heldItem.getData(DataTypes.ID)?.equals("DWARVEN_METAL_DETECTOR") == true
}
