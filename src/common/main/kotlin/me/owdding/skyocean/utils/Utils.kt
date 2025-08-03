package me.owdding.skyocean.utils

import com.google.common.cache.Cache
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import earth.terrarium.olympus.client.components.textbox.TextBox
import kotlinx.coroutines.runBlocking
import me.owdding.ktmodules.AutoCollect
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.SkyOcean.repoPatcher
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.ChatUtils.withoutShadow
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.ItemLike
import org.joml.Vector3dc
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.json.Json
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
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

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
    operator fun String.unaryPlus(): MutableComponent = Component.translatable("skyocean.${this.removePrefix("skyocean.")}")

    /** Translatable Component **without** shadow */
    operator fun String.unaryMinus(): MutableComponent = unaryPlus().withoutShadow()
    operator fun String.not(): MutableComponent = Component.literal(this)

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
        return this.mapNotNull {
            val value = transform(it)

            if (value == null) {
                nullConsumer(it)
            }

            value
        }
    }

    fun applyPatch(json: JsonElement, file: String): JsonElement {
        try {
            repoPatcher?.patch(json, file)
        } catch (e: Exception) {
            SkyOcean.error("Failed to apply patches for file $file", e)
        }
        return json
    }

    inline fun <reified T : Any> loadFromRepo(file: String): T? = runBlocking {
        try {
            val json = SkyOcean.SELF.findPath("repo/$file.json").orElseThrow()?.let(Files::readString)?.readJson<JsonElement>() ?: return@runBlocking null
            applyPatch(json, file)
            if (T::class == JsonElement::class) {
                return@runBlocking json as T
            }
            return@runBlocking Json.gson.fromJson(json, typeOf<T>().javaType)
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

    val ItemLike.id get() = BuiltInRegistries.ITEM.getKey(this.asItem())

    fun TextBox.resetCursor() {
        this.setCursorPosition(0)
        this.setHighlightPos(0)
    }

    fun <T> Iterable<T>.firstOrElseLast(predicate: (T) -> Boolean): T {
        return this.firstOrNull(predicate) ?: this.last()
    }

    fun compoundTag(init: CompoundTag.() -> Unit) = CompoundTag().apply(init)
    fun CompoundTag.putCompound(key: String, init: CompoundTag.() -> Unit) = this.put(key, compoundTag(init))
    fun CompoundTag.toData(): CustomData = CustomData.of(this)

    operator fun <T> ItemBuilder.set(type: DataComponentType<T>, value: T) = this.set(type, value)
    fun itemBuilder(item: ItemLike, init: ItemBuilder.() -> Unit) = ItemBuilder().also { it.item = item.asItem() }.apply(init).build()

    private val validChars = listOf(' ', '_', '-', ':')
    fun String.sanitizeForCommandInput() = this.filter { it.isDigit() || it.isLetter() || it in validChars }.trim()

    fun Component.visitSiblings(visitor: (Component) -> Unit) {
        this.siblings.forEach {
            visitor(it)
            it.visitSiblings(visitor)
        }
    }

    operator fun <Key : Any, Value : Any> Cache<Key, Value>.get(key: Key) = this.getIfPresent(key)
    operator fun <Key : Any, Value : Any> Cache<Key, Value>.set(key: Key, value: Value) = this.put(key, value)
}

@AutoCollect("LateInitModules")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LateInitModule
