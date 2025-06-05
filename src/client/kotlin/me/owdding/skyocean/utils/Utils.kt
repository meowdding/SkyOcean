package me.owdding.skyocean.utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.runBlocking
import me.owdding.skyocean.SkyOcean
import net.minecraft.core.BlockPos
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.joml.Vector3dc
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.readText
import kotlin.io.path.writeText

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

    operator fun Item.contains(stack: ItemStack): Boolean = stack.item == this

    inline fun <reified T> CommandContext<*>.getArgument(name: String): T? = this.getArgument(name, T::class.java)

    @OptIn(ExperimentalContracts::class)
    fun PoseStack.atCamera(task: PoseStack.() -> Unit) {
        contract {
            callsInPlace(task, InvocationKind.EXACTLY_ONCE)
        }

        val camera = McClient.self.gameRenderer.mainCamera
        this.pushPose()
        this.translate(-camera.position.x, -camera.position.y, -camera.position.z)
        this.task()
        this.popPose()
    }

    operator fun BlockPos.plus(vec: Vector3dc) = BlockPos(this.x + vec.x().toInt(), this.y + vec.y().toInt(), this.z + vec.z().toInt())
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

    fun StringReader.peekNext(amount: Int): String = with(StringBuilder()) {
        val current = this@peekNext.cursor
        (0 until (amount)).forEach { _ ->
            append(read())
        }
        cursor = current
    }.toString()
}
