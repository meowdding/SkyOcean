package me.owdding.skyocean.utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import kotlinx.coroutines.runBlocking
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.ChatUtils.withoutShadow
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.joml.Vector3dc
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.math.roundToInt

// TODO: surely better name maybe?
object Utils {
    infix fun Int.exclusiveInclusive(other: Int) = (this + 1)..other
    infix fun Int.exclusiveExclusive(other: Int) = (this + 1)..(other - 1)

    fun Double.roundToHalf(): Double {
        return (this * 2).roundToInt() / 2.0
    }

    operator fun Item.contains(stack: ItemStack): Boolean = stack.item == this

    inline fun <reified T> CommandContext<*>.getArgument(name: String): T? = this.getArgument(name, T::class.java)

    operator fun BlockPos.plus(vec: Vector3dc) = BlockPos(this.x + vec.x().toInt(), this.y + vec.y().toInt(), this.z + vec.z().toInt())

    /** Translatable Component **with** shadow */
    operator fun String.unaryPlus(): MutableComponent = Component.translatable("skyocean.$this")

    /** Translatable Component **without** shadow */
    operator fun String.unaryMinus(): MutableComponent = Component.translatable("skyocean.$this").withoutShadow()
    operator fun BlockPos.plus(vec: BlockPos): BlockPos = this.offset(vec.x, vec.y, vec.z)

    fun Path.readAsJson(): JsonElement = JsonParser.parseString(this.readText())
    fun <T : JsonElement> Path.readJson(): T = this.readAsJson() as T
    fun Path.writeJson(
        element: JsonElement,
        charset: Charset = Charsets.UTF_8,
        vararg options: StandardOpenOption = arrayOf(
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE,
        ),
    ) = this.writeText(element.toPrettyString(), charset, *options)

    inline fun <K, V, R> Map<out K, V>.mapNotNull(nullConsumer: (Map.Entry<K, V>) -> Unit, transform: (Map.Entry<K, V>) -> R?): List<R> {
        return this.mapNotNull { it ->
            val value = transform(it)

            if (value == null) {
                nullConsumer(it)
            }

            value
        }
    }


    inline fun <reified T : Any> loadFromRepo(file: String) = runBlocking {
        try {
            SkyOcean.SELF.findPath("repo/$file.json").orElseThrow()?.let(Files::readString)?.readJson<T>() ?: return@runBlocking null
        } catch (e: Exception) {
            SkyOcean.error("Failed to load $file from repo", e)
            null
        }
    }

    internal inline fun <reified T : Any> loadRepoData(file: String): T {
        return loadRepoData<T, T>(file) { it }
    }

    internal inline fun <reified T : Any, B : Any> loadRepoData(file: String, modifier: (Codec<T>) -> Codec<B>): B {
        return loadFromRepo<JsonElement>(file).toDataOrThrow(SkyOceanCodecs.getCodec<T>().let(modifier))
    }

    internal inline fun <B : Any> loadRepoData(file: String, supplier: () -> Codec<B>): B {
        return loadFromRepo<JsonElement>(file).toDataOrThrow(supplier())
    }

    internal fun <B : Any> loadRepoData(file: String, codec: Codec<B>): B {
        return loadFromRepo<JsonElement>(file).toDataOrThrow(codec)
    }

}
