package me.owdding.skyocean.features.mining.hollows

import me.owdding.ktmodules.Module
import me.owdding.skyocean.features.item.lore.AbstractLoreModifier
import me.owdding.skyocean.features.item.lore.LoreModifier
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.ReplaceMessage
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.extensions.toVec3LowerUpperY
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.chat.ActionBarReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.NameChangedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.level.RightClickEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockAreas
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findGroup
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Suppress("unused")
@Module
object MetalDetectorSolver {

    private const val DETECTOR = "DWARVEN_METAL_DETECTOR"
    private val actionbarDistanceRegex = "TREASURE: (?<distance>[\\d.]+)m".toRegex()
    private val foundTreasureRegex = "You found .* with your Metal Detector!".toRegex()

    private val keeperOffset = mapOf(
        "Gold" to Vec3i(3, 0, -33),
        "Diamond" to Vec3i(33, 0, 3),
        "Emerald" to Vec3i(-3, 0, 33),
        "Lapis" to Vec3i(-33, 0, -3),
    )

    private val offsets = arrayOf(
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
        Vec3i(24, -22, 27),
    )
    private var center: BlockPos? = null
    private val locations = arrayListOf<BlockPos>()

    private val possibleChests = arrayListOf<BlockPos>()
    private var foundChest: BlockPos? = null

    private var distance: Double? = null

    private var cooldown = System.currentTimeMillis()

    private var noChestFoundCounter = 0
    private var previousCurrentChests = -1

    private var distanceOnFind = -1.0

    private var moveCloseToKeeperWarning = true
    private val moveDownMessage = ReplaceMessage("Please move further down to correctly detect treasure.")
    private val resetMessage = ReplaceMessage("Move around some more!")

    /* Coordinate Tracking */
    private var checkingFromPos: PosAndTime? = null
    private var hasntBeenMoving = false

    private fun ItemStack?.isDetector() = this?.getSkyBlockId() == DETECTOR

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun eachSecond(event: TickEvent) {
        val currentPos = McPlayer.position ?: return
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
        val chests = arrayListOf<BlockPos>()
        (possibleChests.takeIf { it.isNotEmpty() } ?: locations).forEach { loc ->
            val dist = distance ?: return@forEach

            val distToLower = pos.distanceTo(loc.toVec3LowerUpperY())
            if (distToLower in dist - 0.1..dist + 0.1) {
                chests.add(loc)
            }
        }
        if (chests.size == 1) {
            // when only one chest is found
            foundChest = chests.first()
            possibleChests.clear()
            Text.of("Chest found at §a${foundChest!!.x}§f, §a${foundChest!!.y}§f, §a${foundChest!!.z}").sendWithPrefix()
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
        if (center != null) return
        if (locations.isNotEmpty()) return
        if (event.infoLineEntity !is ArmorStand || !event.literalComponent.startsWith("Keeper of ")) return

        val keeperPos = event.infoLineEntity.blockPosition()
        val keeperType = event.literalComponent.substringAfter("Keeper of ")
        val offset = keeperOffset[keeperType] ?: return
        val foundCenter = BlockPos(keeperPos.x + offset.x, keeperPos.y + offset.y, keeperPos.z + offset.z)

        center = foundCenter
        offsets.forEach { location ->
            locations.add(BlockPos(location.x + foundCenter.x, location.y + foundCenter.y, location.z + foundCenter.z))
        }
    }

    @Subscription
    fun onServerChange(event: ServerChangeEvent) = fullReset()

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onChatMessage(event: ChatReceivedEvent.Pre) {
        if (foundTreasureRegex.matches(event.text)) {
            reset()
            cooldown = System.currentTimeMillis() + 2500
            McClient.self.gui.overlayMessageString?.stripped?.let { message ->
                distanceOnFind = getDistance(message)
            }
        }
    }

    private fun fullReset() {
        center = null
        locations.clear()
        moveCloseToKeeperWarning = true
        distanceOnFind = -1.0
        reset()
    }

    private fun reset() {
        noChestFoundCounter = 0
        distance = null
        possibleChests.clear()
        foundChest = null
        previousCurrentChests = -1
    }

    // bad loc: 0xFF808080u
    // found: 0xFFFFFF00u

    fun searchCheck(): Boolean =
        SkyBlockAreas.MINES_OF_DIVAN.inArea() && McPlayer.heldItem.isDetector() && foundChest == null && System.currentTimeMillis() > cooldown

    fun getDistance(actionBar: String): Double {
        val split = actionBar.split("     ")
        for (widget in split) {
            actionbarDistanceRegex.findGroup(widget, "distance")?.let {
                return it.toDouble()
            }
        }
        return -1.0
    }

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
        if (event.stack.isDetector() && McPlayer.self!!.isCrouching) {
            Text.of("Resetting Metal Detector Solver").sendWithPrefix()
            reset()
        }
    }

    @LoreModifier
    object MetalDetectorLoreModifier : AbstractLoreModifier() {
        override val displayName: Component = +"skyocean.config.lore_modifiers.metal_detector_lore"
        override val isEnabled: Boolean get() = true

        override fun appliesTo(item: ItemStack): Boolean = item.isDetector()

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
                },
            )
            add(
                Text.of {
                    this.color = TextColor.GRAY
                    append("Reset the solver if it breaks.")
                },
            )
            space()
            true
        }

    }
}
