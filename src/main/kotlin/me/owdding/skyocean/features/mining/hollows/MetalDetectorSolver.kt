package me.owdding.skyocean.features.mining.hollows

import me.owdding.ktmodules.Module
import me.owdding.lib.waypoints.ExpellingWaypoint
import me.owdding.lib.waypoints.ExpellingWaypointList
import me.owdding.lib.waypoints.MeowddingWaypoint
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.ReplaceMessage
import me.owdding.skyocean.utils.extensions.toVec3LowerUpperY
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
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
        Vec3i(-24, -22, 12), Vec3i(-42, -20, -28), Vec3i(30, -21, -25), Vec3i(40, -22, -30), Vec3i(-14, -21, 43), Vec3i(-23, -22, 40),
        Vec3i(23, -22, -39), Vec3i(42, -19, -41), Vec3i(6, -21, 28), Vec3i(-43, -22, -40), Vec3i(-12, -21, -44), Vec3i(12, -21, -43),
        Vec3i(25, -22, 17), Vec3i(20, -21, -26), Vec3i(-37, -21, -14), Vec3i(1, -21, 20), Vec3i(-5, -21, 16), Vec3i(22, -21, -14),
        Vec3i(7, -21, 22), Vec3i(-40, -22, 18), Vec3i(12, -21, 7), Vec3i(-1, -22, -20), Vec3i(38, -22, -26), Vec3i(12, -22, 31),
        Vec3i(-20, -22, 0), Vec3i(43, -21, -16), Vec3i(-17, -21, 20), Vec3i(-31, -21, -40), Vec3i(29, -21, -44), Vec3i(20, -22, 0),
        Vec3i(-14, -21, 22), Vec3i(12, -22, -22), Vec3i(-37, -21, -22), Vec3i(7, -21, 11), Vec3i(-36, -20, 42), Vec3i(19, -22, 29),
        Vec3i(-38, -22, 26), Vec3i(-31, -21, -12), Vec3i(24, -22, 27),
    )
    private var center: BlockPos? = null
    private val locations = arrayListOf<BlockPos>()
    private var possibleChests by ExpellingWaypointList()
    private var foundChest by ExpellingWaypoint()
    private var distance: Double? = null
    private var cooldown = System.currentTimeMillis()
    private var noChestFoundCounter = 0
    private var previousCurrentChests = -1
    private var distanceOnFind = -1.0
    private var moveCloseToKeeperWarning = true
    private val moveDownMessage = ReplaceMessage("Please move further down to correctly detect treasure.")
    private val resetMessage = ReplaceMessage("Move around some more!")
    private var checkingFromPos: PosAndTime? = null
    private var hasntBeenMoving = false

    private fun ItemStack?.isDetector() = this?.getSkyBlockId() == DETECTOR

    private fun check(pos: Vec3) {
        if (distance == null) resetMessage.send()

        val possibleLocations = possibleChests.takeIf { it.isNotEmpty() } ?: locations.map {
            MeowddingWaypoint(it, false) {
                withName(Text.of("Possible Treasure").withColor(TextColor.GRAY))
                withColor(0xFF808080.toInt())
                withAllRenderTypes()
                inLocatorBar()
            }
        }
        val candidates = possibleLocations.filter {
            val distToLower = pos.distanceTo(it.getBlockPos().toVec3LowerUpperY())
            val distance = distance ?: return@filter false
            distToLower in distance - 0.1..distance + 0.1
        }

        when (candidates.size) {
            0 -> {
                noChestFoundCounter++
                if (noChestFoundCounter > 2) {
                    resetMessage.send()
                    reset()
                }
            }

            1 -> {
                foundPosition(candidates.first().getBlockPos())
                possibleChests = mutableListOf()
            }

            else -> {
                possibleChests.run {
                    if (isNotEmpty()) {
                        retainAll(candidates)
                        if (size == 1) foundPosition(first().getBlockPos())
                    } else addAll(candidates)
                }
                @Suppress("SelfAssignment") // this needs this, cry about it
                possibleChests = possibleChests
                if (possibleChests.size != previousCurrentChests) {
                    Text.of("Found §c${possibleChests.size}§f chests, Keep moving around!").sendWithPrefix()
                }
                previousCurrentChests = possibleChests.size
            }
        }
    }

    private fun foundPosition(blockPos: BlockPos) {
        foundChest = MeowddingWaypoint(blockPos, false) {
            withName(Text.of("Treasure").withColor(TextColor.ORANGE))
            withColor(0xFFFFFF00.toInt())
            withAllRenderTypes()
            inLocatorBar()
        }
        Text.of("Chest found at §a${blockPos.x}§f, §a${blockPos.y}§f, §a${blockPos.z}").sendWithPrefix()
        if (MiningConfig.playDingOnFind) McPlayer.self?.playSound(SoundEvents.NOTE_BLOCK_PLING.value())
        if (MiningConfig.showTitleOnFind) {
            val gui = McClient.self.gui
            gui.setTimes(5, 20, 5)
            gui.setSubtitle(Text.of("§a${blockPos.x}§f, §a${blockPos.y}§f, §a${blockPos.z}"))
            gui.setTitle(Text.of("Treasure Found!").withColor(TextColor.YELLOW))
        }
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onActionBar(event: ActionBarReceivedEvent.Pre) {
        if (!isEnabled()) return
        val distanceFromActionbar = event.text.split("     ").firstNotNullOfOrNull {
            actionbarDistanceRegex.findGroup(it, "distance")?.toDoubleOrNull()
        } ?: -1.0
        if (distance != -1.0 && distanceFromActionbar != distanceOnFind) {
            distanceOnFind = -1.0
            cooldown = System.currentTimeMillis() - 10000
        }
        if (searchCheck() && distanceFromActionbar != 1.0) distance = distanceFromActionbar
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onEntityNamed(event: NameChangedEvent) {
        if (center != null || locations.isNotEmpty()) return
        if (event.infoLineEntity !is ArmorStand || !event.literalComponent.startsWith("Keeper of ")) return

        val keeperType = event.literalComponent.removePrefix("Keeper of ")
        val offset = keeperOffset[keeperType] ?: return
        val keeperPos = event.infoLineEntity.blockPosition()
        center = BlockPos(keeperPos.x + offset.x, keeperPos.y + offset.y, keeperPos.z + offset.z).also { center ->
            offsets.mapTo(locations) { BlockPos(it.x + center.x, it.y + center.y, it.z + center.z) }
        }
    }

    @Subscription
    fun onServerChange(event: ServerChangeEvent) = fullReset()

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onChatMessage(event: ChatReceivedEvent.Pre) {
        if (!isEnabled()) return
        if (foundTreasureRegex.matches(event.text)) {
            reset()
            cooldown = System.currentTimeMillis() + 2500
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

    private fun isEnabled() = MiningConfig.metalDetectorSolver && SkyBlockAreas.MINES_OF_DIVAN.inArea()
    private fun searchCheck() = isEnabled() && McPlayer.heldItem.isDetector() && foundChest == null && System.currentTimeMillis() > cooldown

    private fun getDistance(actionBar: String): Double = actionBar.split("     ").firstNotNullOfOrNull {
        actionbarDistanceRegex.findGroup(it, "distance")?.toDoubleOrNull()
    } ?: -1.0

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onTick(event: TickEvent) {
        if (!isEnabled()) return
        val playerPosition = McPlayer.position ?: return

        onTickMove(playerPosition)
        onTickWarning(playerPosition)
    }

    private fun onTickMove(playerPosition: Vec3) {
        if (!isEnabled()) return

        checkingFromPos?.let {
            if (System.currentTimeMillis() > it.time + 1000) {
                if (it.position == playerPosition) {
                    hasntBeenMoving = true
                } else {
                    checkingFromPos = null
                    hasntBeenMoving = false
                }
            }
        } ?: run {
            checkingFromPos = PosAndTime(System.currentTimeMillis(), playerPosition)
        }
    }

    private fun onTickWarning(playerPosition: Vec3) {
        if (!searchCheck()) return

        center?.let {
            if (playerPosition.y > it.y - 2) moveDownMessage.send()
            else if (hasntBeenMoving) check(playerPosition)
        } ?: run {
            if (moveCloseToKeeperWarning) {
                moveCloseToKeeperWarning = false
                Text.of("Move close to a keeper to get proper locations").sendWithPrefix()
            }
        }
    }

    class PosAndTime(val time: Long, val position: Vec3)

    @Subscription
    fun onRightClick(event: RightClickEvent) {
        if (!isEnabled()) return
        if (event.stack.isDetector() && McPlayer.self?.isCrouching == true) {
            Text.of("Resetting Metal Detector Solver").sendWithPrefix()
            reset()
        }
    }

    @ItemModifier
    object MetalDetectorLoreModifier : AbstractItemModifier() {
        override val displayName: Component = +"skyocean.config.mining.metal_detector.metalDetector"
        override val isEnabled: Boolean get() = MiningConfig.metalDetectorSolver

        override fun appliesTo(itemStack: ItemStack): Boolean = itemStack.isDetector()

        override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
            addUntil { it.stripped == "SPECIAL" }
            add(
                Text.of {
                    append("Ability: Solver Reset ").withColor(TextColor.ORANGE)
                    append("SNEAK RIGHT CLICK") {
                        color = TextColor.YELLOW
                        bold = true
                    }
                },
            )
            add(
                Text.of {
                    color = TextColor.GRAY
                    append("Reset the solver if it breaks.")
                },
            )
            space()
            Result.modified
        }
    }
}
