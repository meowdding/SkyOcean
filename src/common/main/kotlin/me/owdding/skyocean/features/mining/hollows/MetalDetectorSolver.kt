package me.owdding.skyocean.features.mining.hollows

import me.owdding.ktmodules.Module
import me.owdding.skyocean.features.item.lore.AbstractLoreModifier
import me.owdding.skyocean.features.item.lore.LoreModifier
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.StaticMessageWithCooldown
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.Waypoint
import me.owdding.skyocean.utils.extensions.toBlockPos
import me.owdding.skyocean.utils.extensions.toVec3LowerUpperY
import me.owdding.skyocean.utils.rendering.RenderUtils.renderBox
import me.owdding.skyocean.utils.rendering.RenderUtils.renderLineFromCursor
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.chat.ActionBarReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.NameChangedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.RightClickEvent
import tech.thatgravyboat.skyblockapi.api.events.render.RenderWorldEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockAreas
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.time.Duration.Companion.seconds

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

    var foundChest: Waypoint? = null
    // Gets populated once per lobby
    val locations = arrayListOf<Waypoint>()

    val possibleChests = arrayListOf<Waypoint>()

    var distance: Double? = null
    var center: BlockPos? = null

    var cooldown = System.currentTimeMillis()

    var noChestFoundCounter = 0
    var previousCurrentChests = -1

    var distanceOnFind = -1.0

    val moveDownMessage = StaticMessageWithCooldown(5.seconds, Text.of("Please move further down to correctly detect treasure."))
    var moveCloseToKeeperWarning = true
    val resetMessage = StaticMessageWithCooldown(4.seconds, Text.of("Move around some more!"))

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun eachSecond(event: TickEvent) {
        val currentPos = McPlayer.position?: return
        if (!searchCheck()) return
        if (center != null) {
            if (currentPos.y > center!!.y - 2) {
                moveDownMessage.send()
            } else if (hasntBeenMoving) {
                check(currentPos)
            }
        } else if (moveCloseToKeeperWarning) {
            moveCloseToKeeperWarning = false
            Text.of("Move close to a keeper to get proper locations").sendWithPrefix()
        }
    }

    private fun check(pos: Vec3) {
        if (distance == null) {
            resetMessage.send()
        }
        val chests = arrayListOf<Waypoint>()
        (possibleChests.takeIf { it.isNotEmpty() } ?: locations).forEach { loc ->
            val dist = distance ?: return@forEach

            val distToLower = pos.distanceTo(loc.pos.toBlockPos().toVec3LowerUpperY())
            if (distToLower in dist-0.1..dist+0.1) {
                chests.add(loc)
            }
        }
        if (chests.size == 1) {
            // when only one chest is found
            foundChest = chests.first()
            possibleChests.clear()
            Text.of("Chest found at §a${chests[0].pos.x.toInt()}§f, §a${chests[0].pos.y.toInt()}§f, §a${chests[0].pos.z.toInt()}").sendWithPrefix()
        } else if (chests.isEmpty()) {
            // when no chest is found
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
                Text.of("Found §c${possibleChests.size}§f chests, Keep moving around!").sendWithPrefix()
            }
            previousCurrentChests = possibleChests.size
        }
        if (noChestFoundCounter > 2) {
            resetMessage.send()
            reset()
        }
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onActionBar(event: ActionBarReceivedEvent.Pre) {
        if (distance != -1.0) {
            if (getDistance(event.text) != distanceOnFind) {
                distanceOnFind = -1.0
                cooldown = System.currentTimeMillis() - 10000
            }
        }
        if (searchCheck()) {
            val distance = getDistance(event.text)
            if (distance != -1.0) {
                this.distance = distance
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
                            BlockPos(location.x + newPos.x, location.y + newPos.y, location.z + newPos.z).toVec3LowerUpperY(),
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
        locations.clear()
        moveCloseToKeeperWarning = true
        distanceOnFind = -1.0
        reset()
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onChatMessage(event: ChatReceivedEvent.Pre) {
        if (event.text.startsWith("You found") && event.text.endsWith("with your Metal Detector!")) {
            reset()
            cooldown = System.currentTimeMillis() + 2500
            McClient.self.gui.overlayMessageString?.stripped?.let { message ->
                distanceOnFind = getDistance(message)
            }
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
                event.renderBox(chest.pos.toBlockPos().below(), 0xFF808080u)
            }
            //chest.render(event.poseStack, event.camera, event.buffer)
            event.renderLineFromCursor(chest.pos.add(0.5, -1.5, 0.5), 0xFF808080u, 0.3F)
        }
        foundChest.takeIf { it != null }?.let { chest ->
            event.atCamera {
                event.renderBox(chest.pos.toBlockPos().below(), 0xFFFFFF00u)
            }
            //chest.render(event.poseStack, event.camera, event.buffer)
            event.renderLineFromCursor(chest.pos.add(0.5, -1.5, 0.5), 0xFFFFFF00u, 1.0F)
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

    fun getDistance(actionBar: String): Double {
        val split = actionBar.split("     ")
        for (widget in split) {
            if (widget.matches(treasureRegex)) {
                treasureRegex.matchEntire(widget)?.let {
                    return it.groupValues[1].toDouble()
                }
            }
        }
        return -1.0
    }

    /* Coordinate Tracking */
    var checkingFromPos: PosAndTime? = null
    var hasntBeenMoving = false

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onTick(event: TickEvent) {
        val delta = McPlayer.self?.deltaMovement ?: return
        if (delta.x == 0.0 && delta.z == 0.0 && McPlayer.self!!.onGround()) {
            if (checkingFromPos == null) {
                checkingFromPos = PosAndTime(System.currentTimeMillis(), McPlayer.self!!.position())
            } else {
                if (System.currentTimeMillis() > checkingFromPos!!.time + 1000) {
                    if (checkingFromPos!!.pos == McPlayer.self!!.position()) {
                        hasntBeenMoving = true
                    } else {
                        checkingFromPos = null
                        hasntBeenMoving = false
                    }
                }
            }
        } else {
            checkingFromPos = null
            hasntBeenMoving = false
        }
    }

    class PosAndTime(val time: Long, val pos: Vec3)

    @Subscription
    fun onRightClick(event: RightClickEvent) {
        if (event.stack.getData(DataTypes.ID) == "DWARVEN_METAL_DETECTOR" && McPlayer.self!!.isCrouching) {
            Text.of("Resetting Metal Detector Solver").sendWithPrefix()
            reset()
        }
    }

    @LoreModifier
    object MetalDetectorLoreModifier : AbstractLoreModifier() {
        override val displayName: Component = +"skyocean.config.lore_modifiers.metal_detector_lore"
        override val isEnabled: Boolean get() = true

        override fun appliesTo(item: ItemStack): Boolean = item.getData(DataTypes.ID) == "DWARVEN_METAL_DETECTOR"

        override fun modify(
            item: ItemStack,
            list: MutableList<Component>,
        ): Boolean = withMerger(list) {
            addUntil { it.stripped == "SPECIAL" }
            add(
                Text.of {
                    append("Ability: Solver Reset ").withColor(TextColor.ORANGE)
                    append("SNEAK RIGHT CLICK") {
                        this.color = TextColor.YELLOW
                        this.bold = true
                    }
                }
            )
            add(
                Text.of {
                    this.color = TextColor.GRAY
                    append("Reset the solver if it breaks.")
                }
            )
            space()
            true
        }

    }
}
