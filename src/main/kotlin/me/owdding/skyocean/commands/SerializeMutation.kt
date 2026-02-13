package me.owdding.skyocean.commands

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.Utils.plus
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.minecraft.commands.arguments.blocks.BlockStateParser
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.structure.BoundingBox
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.platform.identifier
import tech.thatgravyboat.skyblockapi.utils.extentions.filterKeysNotNull
import tech.thatgravyboat.skyblockapi.utils.json.JsonArray
import tech.thatgravyboat.skyblockapi.utils.json.JsonObject
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick
import kotlin.math.max
import kotlin.math.min

@Module
object SerializeMutation {

    private val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
    private val paletteChars: String = " #*+-?!123456789=$%&/abcdefghijklmnopqrstuvwxyz"

    @Subscription
    fun registerCommands(event: RegisterSkyOceanCommandEvent) {
        event.registerDevWithCallback("serialize_mutation") {
            val corner = BlockPos.betweenClosedStream(
                BoundingBox(McPlayer.self!!.blockPosition()).inflatedBy(5),
            ).map { it.immutable() }.filter { McLevel[it].block == Blocks.GOLD_BLOCK }.toList().takeUnless { it.size != 1 }?.first()

            if (corner == null) {
                Text.of("Couldn't identify corner block!").sendWithPrefix()
                return@registerDevWithCallback
            }

            var size = 0
            while (McLevel[corner.offset(size + 1, 0, 0)].block == Blocks.LIME_CONCRETE) size++
            Text.of("Detected size ${size}x${size}x${size}").sendWithPrefix()

            var minX = size
            var minY = size
            var minZ = size
            var maxX = 0
            var maxY = 0
            var maxZ = 0

            BlockPos.betweenClosed(BlockPos(0, 1, 0), BlockPos(size, 1 + size, size)).forEach {
                if (!McLevel[corner.plus(it)].isAir) {
                    minX = min(it.x, minX)
                    minY = min(it.y, minY)
                    minZ = min(it.z, minZ)
                    maxX = max(it.x, maxX)
                    maxY = max(it.y, maxY)
                    maxZ = max(it.z, maxZ)
                }
            }


            val ySize = maxY - minY + 1
            val xSize = maxX - minX + 1
            val zSize = maxZ - minZ + 1
            val start = corner.offset(minX, minY, minZ)
            val map: MutableMap<BlockState?, Char> = mutableMapOf()
            map[null] = ' '
            var last = JsonArray()
            val layers = JsonArray()
            var current = StringBuilder()
            for (y in (ySize - 1) downTo 0) {
                for (x in 0 until xSize) {
                    for (z in 0 until zSize) {
                        current.append(
                            map.getOrPut(McLevel[start.offset(x, y, z)].takeUnless { it.isAir }) {
                                paletteChars[map.size]
                            },
                        )
                    }
                    last.add(current.toString())
                    current = StringBuilder()
                }
                layers.add(last)
                last = JsonArray()
            }

            val blueprint = JsonObject()
            blueprint.add(
                "palette",
                JsonObject {
                    map.filterKeysNotNull().forEach { (state, string) ->
                        this[string.toString()] = BlockStateParser.serialize(state).takeUnless {
                            state.block.defaultBlockState() == state
                        } ?: state.block.builtInRegistryHolder().key().identifier.toString()
                    }
                },
            )
            blueprint.add("shape", layers)
            blueprint.add(
                "size",
                JsonArray {
                    add(xSize)
                    add(ySize)
                    add(zSize)
                },
            )
            Text.of("Serialized shape! Click to copy!") {
                onClick {
                    McClient.clipboard = gson.toJson(blueprint)
                }
            }.sendWithPrefix()
        }
    }

}
