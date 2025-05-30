package me.owdding.skyocean.utils

import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.runBlocking
import me.owdding.skyocean.SkyOcean
import net.minecraft.core.BlockPos
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.joml.Vector3dc
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import java.nio.file.Files
import kotlin.math.roundToInt

// TODO: surely better name maybe?
object Utils {
    inline fun <reified T : Any> loadFromRepo(file: String) = runBlocking {
        try {
            SkyOcean.SELF.findPath("repo/$file.json").orElseThrow()?.let(Files::readString)?.readJson<T>() ?: return@runBlocking null
        } catch (e: Exception) {
            SkyOcean.error("Failed to load $file from repo", e)
            null
        }
    }


    infix fun Int.exclusiveInclusive(other: Int) = (this + 1)..other
    infix fun Int.exclusiveExclusive(other: Int) = (this + 1)..(other - 1)

    fun Double.roundToHalf(): Double {
        return (this * 2).roundToInt() / 2.0
    }

    operator fun Item.contains(stack: ItemStack): Boolean = stack.item == this

    inline fun <reified T> CommandContext<*>.getArgument(name: String): T? = this.getArgument(name, T::class.java)

    operator fun BlockPos.plus(vec: Vector3dc) = BlockPos(this.x + vec.x().toInt(), this.y + vec.y().toInt(), this.z + vec.z().toInt())
}
