package me.owdding.skyocean.features.mining.hollows

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.ChatUtils
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.Waypoint
import me.owdding.skyocean.utils.rendering.RenderUtils
import me.owdding.skyocean.utils.rendering.RenderUtils.renderBox
import me.owdding.skyocean.utils.rendering.RenderUtils.renderLineFromCursor
import me.owdding.skyocean.utils.toBlockPos
import me.owdding.skyocean.utils.toVec3Lower
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
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.NameChangedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockAreas
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.awt.Color

@Suppress("unused")
@Module
object MetalDetectorSolver {

    val treasureRegex = Regex("""TREASURE: (\d+\.\d+)m""")

    val keeperOffset = mapOf(
        "Gold" to Vec3i(3, 0, -33),
        "Diamond" to Vec3i(33, 0, 3),
        "Emerald" to Vec3i(-3, 0, 33),
        "Lapis" to Vec3i(-33, 0, -3),
    )

    val offsets = arrayOf(
        Vec3i(-20, -22, 0),
        Vec3i(-31, -21, -40),
        Vec3i(29, -21, -44),
        Vec3i(-37, -21, -22),
        Vec3i(-24, -22, 12),
        Vec3i(43, -21, -16),
        Vec3i(12, -21, -43),
        Vec3i(6, -21, 28),
        Vec3i(-40, -22, 18),
        Vec3i(38, -22, -26),
        Vec3i(30, -21, -25),
        Vec3i(-37, -21, -14),
        Vec3i(42, -19, -41),
        Vec3i(24, -22, 27),
        Vec3i(7, -21, 22),
        Vec3i(-14, -21, 43),
        Vec3i(12, -22, -22),
        Vec3i(12, -22, 31),
        Vec3i(-36, -20, 42),
        Vec3i(-38, -22, 26),
        Vec3i(25, -22, 17),
        Vec3i(20, -22, 0),
        Vec3i(-23, -22, 40),
        Vec3i(19, -22, 29),
        Vec3i(23, -22, -39),
        Vec3i(22, -21, -14),
        Vec3i(-14, -21, 22),
        Vec3i(12, -21, 7),
        Vec3i(-43, -22, -40),
        Vec3i(-5, -21, 16),
        Vec3i(-1, -22, -20),
        Vec3i(-44, -21, -14),
        Vec3i(-29, -21, -16),
        Vec3i(-12, -21, -44),
        Vec3i(-31, -21, -12),
        Vec3i(7, -21, 11),
        Vec3i(-42, -20, -28),
        Vec3i(1, -21, 20),
        Vec3i(20, -21, -26),
        Vec3i(40, -22, -30),
    )


    var foundChest: Waypoint? = null
    // Gets populated once per lobby
    val locations = arrayListOf<Waypoint>()

    val possibleChests = arrayListOf<Waypoint>()

    var distance: Double? = null
    var center: BlockPos? = null

    var oldPos: Vec3 = Vec3.ZERO

    var cooldown = System.currentTimeMillis()

    var noChestFoundCounter = 0
    var previousCurrentChests = -1

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    @TimePassed("1s")
    fun eachSecond(event: TickEvent) {
        val currentPos = McPlayer.position?: return
        if (!searchCheck()) return
        if (center != null) {
            if (currentPos.y > center!!.y - 2) {
                Text.of("Please move further down to correctly detect treasure.").sendWithPrefix()
            }
            if (oldPos == currentPos) {
                check(getPlayerPosAdjustedForEyeHeight(currentPos))
            }
        } else {
            Text.of("Move close to a keeper to get proper locations").sendWithPrefix()
        }
        oldPos = currentPos
    }

    private fun check(pos: Vec3) {
        SkyOcean.info("Checking ${pos.x}, ${pos.y}, ${pos.z} with distance of ${distance ?: 0.0}")
        if (distance == null) {
            Text.of("Move around some more!").sendWithPrefix()
        }
        val chests = arrayListOf<Waypoint>()
        (possibleChests.takeIf { it.isNotEmpty() } ?: locations).forEach { loc ->
            val dist = distance ?: return@forEach

            val distToLower = pos.distanceTo(loc.pos.toBlockPos().toVec3Lower())
            if (distToLower in dist-1.0..dist+1.0) {
                chests.add(loc)
                SkyOcean.info("Found chest at {} {} {} with distance {}", loc.pos.x, loc.pos.y, loc.pos.z, distToLower)
            }
        }
        if (chests.size == 1) {
            // when only one chest is found
            foundChest = chests.first()
            possibleChests.clear()
            Text.of("Chest found at ${chests[0].pos.x}, ${chests[0].pos.y}, ${chests[0].pos.z}").sendWithPrefix()
        } else if (chests.isEmpty()) {
            // when no chest is found
            SkyOcean.info("No chests found")
            noChestFoundCounter++
        } else {
            // when few chests are found
            if (possibleChests.isNotEmpty()) {
                val combined = possibleChests.intersect(chests)
                possibleChests.clear()
                possibleChests.addAll(combined)
                if (possibleChests.size == 1) {
                    foundChest = chests.first()
                    return
                }
            } else {
                possibleChests.addAll(chests)
            }
            if (possibleChests.size != previousCurrentChests) {
                Text.of("Found ${possibleChests.size} chests, Keep moving around!").sendWithPrefix()
            }
            previousCurrentChests = possibleChests.size
        }
        if (noChestFoundCounter > 2) {
            Text.of("No chests found, resetting").sendWithPrefix()
            reset()
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
                        distance = it.groupValues[1].toDouble()
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
                    offsets.forEach { location ->
                        locations.add(Waypoint(
                            Text.of("Treasure"),
                            BlockPos(location.x + newPos.x, location.y + newPos.y, location.z + newPos.z).toVec3Lower(),
                            0.8F,
                            true,
                            box = true
                        ))
                    }
                }
            }
        }
    }

    @Subscription
    fun onServerChange(event: ServerChangeEvent) {
        center = null
        oldPos = Vec3.ZERO
        locations.clear()
        reset()
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onChatMessage(event: ChatReceivedEvent.Pre) {
        if (event.text.startsWith("You found") && event.text.endsWith("with your Metal Detector!")) {
            reset()
            cooldown = System.currentTimeMillis() + 2500
        }
    }

    fun reset() {
        noChestFoundCounter = 0
        distance = null
        possibleChests.clear()
        foundChest = null
        previousCurrentChests = -1
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onRender(event: RenderWorldEvent.AfterEntities) {
        if (LocationAPI.area != SkyBlockAreas.MINES_OF_DIVAN) return
        possibleChests.forEach { chest ->
            event.atCamera {
                event.renderBox(chest.pos.toBlockPos(), 0xFFFF0000u)
            }
            chest.render(event.poseStack, event.camera, event.buffer)
            event.renderLineFromCursor(chest.pos.add(0.5, 0.5, 0.5), 0xFFFF0000u, 0.3F)
        }
        foundChest.takeIf { it != null }?.let { chest ->
            event.atCamera {
                event.renderBox(chest.pos.toBlockPos(), 0xFFFFFF00u)
            }
            chest.render(event.poseStack, event.camera, event.buffer)
            event.renderLineFromCursor(chest.pos.add(0.5, 0.5, 0.5), 0xFFFFFF00u, 1.0F)
        }
    }

    fun searchCheck(): Boolean = LocationAPI.area == SkyBlockAreas.MINES_OF_DIVAN && McPlayer.heldItem.getData(DataTypes.ID)?.equals("DWARVEN_METAL_DETECTOR") == true && foundChest == null && System.currentTimeMillis() > cooldown

    private fun getPlayerPosAdjustedForEyeHeight(playerPos: Vec3): Vec3 {
        return Vec3(
            playerPos.x,
            playerPos.y + McPlayer.self!!.eyeHeight,
            playerPos.z,
        )
    }
}
