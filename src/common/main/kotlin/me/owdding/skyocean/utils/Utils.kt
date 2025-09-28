package me.owdding.skyocean.utils

import com.google.common.cache.Cache
import com.google.common.cache.CacheLoader
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import earth.terrarium.olympus.client.components.textbox.TextBox
import kotlinx.coroutines.runBlocking
import me.owdding.ktmodules.AutoCollect
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.ListMerger
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.SkyOcean.repoPatcher
import me.owdding.skyocean.accessors.SafeMutableComponentAccessor
import me.owdding.skyocean.config.Config
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.withoutShadow
import net.fabricmc.fabric.api.tag.client.v1.ClientTags
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.ItemLike
import org.joml.Vector3dc
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.json.Json
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.inputStream
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.jvm.optionals.getOrNull
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

@Suppress("ClassOrdering")
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

    fun loadFromResourcesAsStream(path: String): InputStream = runBlocking {
        SkyOcean.SELF.findPath(path).orElseThrow().inputStream()
    }

    fun loadFromResources(path: String): ByteArray = loadFromResourcesAsStream(path).readAllBytes()

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
    fun itemBuilder(item: ItemStack, init: ItemBuilder.() -> Unit) = ItemBuilder().apply { copyFrom(item) }.apply(init).build()

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

    fun MutableComponent.appendSafe(other: Component): MutableComponent? = (this as? SafeMutableComponentAccessor)?.`skyocean$appendSafe`(other)
    fun MutableComponent.mutableSiblings(): MutableList<Component>? = (this as? SafeMutableComponentAccessor)?.`skyocean$mutableSiblings`()
    var MutableComponent.textContents: ComponentContents
        get() = this.contents
        set(value) {
            (this as? SafeMutableComponentAccessor)?.`skyocean$setContents`(value)
        }

    inline fun <T> KMutableProperty0<T>.setIf(value: T, predicate: (T) -> Boolean) {
        if (predicate(value)) this.set(value)
    }

    inline fun <T> KMutableProperty0<T>.setIfNot(value: T, predicate: (T) -> Boolean) {
        if (!predicate(value)) this.set(value)
    }

    fun String.replaceTrim(regex: Regex, replacement: String = "") = this.replace(regex, replacement).trim()
    fun String.replaceTrim(oldValue: String, newValue: String = "") = this.replace(oldValue, newValue).trim()

    fun TooltipBuilder.copyFrom(itemStack: ItemStack) = lines().addAll(itemStack.getLore())
    fun MutableComponent.wrap(wrap: String) = this.wrap(wrap, wrap)

    context(_: ItemStack) fun ItemBuilder.skyOceanIndicator() = when (Config.modifyIndicator) {
        SkyOceanModifyIndicator.PREFIX -> this.namePrefix(ChatUtils.ICON_SPACE_COMPONENT)
        SkyOceanModifyIndicator.SUFFIX -> this.nameSuffix(ChatUtils.SPACE_ICON_COMPONENT)
        SkyOceanModifyIndicator.LORE -> this.alterTooltip {
            lines().add(0, Text.of("Modified by SkyOcean").withColor(TextColor.DARK_GRAY))
            lines().add(1, CommonComponents.EMPTY)
        }

        SkyOceanModifyIndicator.NOTHING -> {}
    }

    fun <T, Z> List<T>.mapToMutableList(converter: (T) -> Z) = this.map(converter).toMutableList()

    inline fun ItemStack.skyoceanReplace(addIndicator: Boolean = true, crossinline init: context(ItemStack) ItemBuilder.() -> Unit) {
        this.replaceVisually {
            copyFrom(this@skyoceanReplace)
            init()
            if (addIndicator) skyOceanIndicator()

            set(DataComponents.TOOLTIP_DISPLAY, this@skyoceanReplace.get(DataComponents.TOOLTIP_DISPLAY)?.hiddenComponents()?.let { TooltipDisplay(false, it) })
        }
    }

    context(_: ItemStack) inline fun ItemBuilder.alterTooltip(crossinline init: TooltipBuilder.() -> Unit) {
        this.tooltip {
            lines().addAll(this@alterTooltip.build().getLore())
            init()
        }
    }

    @Suppress("SpacingAroundColon")
    context(original: ItemStack) inline fun ItemBuilder.modifyTooltip(crossinline init: TooltipBuilder.() -> Unit) {
        this.tooltip {
            lines().addAll(original.getLore())
            init()
        }
    }

    @Suppress("SpacingAroundColon")
    context(original: ItemStack) inline fun ItemBuilder.mergeTooltip(crossinline init: ListMerger<Component>.() -> Unit) {
        val merger = ListMerger(original.getLore())
        merger.init()
        tooltip { lines().addAll(merger.destination) }
    }

    fun TooltipBuilder.addAll(iterable: Collection<Component>) = lines().addAll(iterable)
    fun ListMerger<Component>.space() = add(CommonComponents.EMPTY)
    fun ListMerger<Component>.add(init: MutableComponent.() -> Unit) = add(Text.of(init))
    fun ListMerger<Component>.add(text: String, init: MutableComponent.() -> Unit = {}) = add(Text.of(text, init))
    fun ListMerger<Component>.addAll(iterable: Collection<Component>) = this.destination.addAll(iterable)
    fun ListMerger<*>.skipRemaining() {
        while (this.canRead()) read()
    }

    fun Screen?.rebuild() {
        this?.resize(McClient.self, this.width, this.height)
    }

    fun jsonObject(init: context(JsonObject) () -> Unit) = JsonObject().apply(init)
    fun jsonArray(init: context(JsonArray) () -> Unit) = JsonArray().apply(init)

    context(parent: JsonObject) fun putString(property: String, value: String) = parent.addProperty(property, value)
    context(parent: JsonObject) fun putNumber(property: String, value: Number) = parent.addProperty(property, value)
    context(parent: JsonObject) fun putBoolean(property: String, value: Boolean) = parent.addProperty(property, value)
    context(parent: JsonObject) fun putChar(property: String, value: Char) = parent.addProperty(property, value)
    context(parent: JsonObject) fun putElement(property: String, value: JsonElement) = parent.add(property, value)

    context(parent: JsonArray) fun putString(value: String) = parent.add(value)
    context(parent: JsonArray) fun putNumber(value: Number) = parent.add(value)
    context(parent: JsonArray) fun putBoolean(value: Boolean) = parent.add(value)
    context(parent: JsonArray) fun putChar(value: Char) = parent.add(value)

    context(parent: JsonArray) fun putArray(init: context(JsonArray) () -> Unit) = parent.add(JsonArray().apply(init))
    context(parent: JsonObject) fun putArray(property: String, init: context(JsonArray) () -> Unit) = parent.add(property, JsonArray().apply(init))

    context(parent: JsonArray) fun putObject(init: context(JsonObject) () -> Unit) = parent.add(JsonObject().apply(init))
    context(parent: JsonObject) fun putObject(property: String, init: context(JsonObject) () -> Unit) = parent.add(property, JsonObject().apply(init))

    fun List<Slot>.container() = this.filterNot { it.container is Inventory }
    fun List<Slot>.containerItems() = this.filterNot { it.container is Inventory }.map { it.item }

    fun <T : Any, V : Any> simpleCacheLoader(constructor: (T) -> V) = object : CacheLoader<T, V>() {
        override fun load(key: T): V = constructor(key)
    }

    fun text(text: String = "", init: MutableComponent.() -> Unit = {}) = Text.of(text, init)

    fun Component.wrapWithNotItalic() = Text.of {
        append(this@wrapWithNotItalic)
        this.italic = false
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, V> V.unsafeCast(): T = this as T

    @JvmStatic
    fun <T> nonNullElse(value: T?, default: T?): T? {
        return value ?: default
    }

    @JvmStatic
    fun <T> nonNullElseGet(value: T?, default: () -> T?): T? {
        return value ?: default()
    }

    fun <T> TagKey<T>.listEntries() = ClientTags.getOrCreateLocalTag(this).map { this.registry().get(it) }.unwrap()
    fun <T> ResourceKey<out Registry<T>>.list(): List<T> = this.lookup().listElements().map { it.value() }.toList()
    fun <T> ResourceKey<T>.get(): Holder<T>? = SkyOcean.registryLookup.get(this).getOrNull()
    fun <T> ResourceKey<out Registry<T>>.lookup(): HolderLookup.RegistryLookup<T> = SkyOcean.registryLookup.lookupOrThrow(this)
    fun <T> ResourceKey<out Registry<T>>.get(value: T): Holder<T> = this.lookup().filterElements { it == value }.listElements().findFirst().orElseThrow()
    fun <T> ResourceKey<out Registry<T>>.get(value: ResourceLocation): Holder<T> = runCatching {
        this.lookup().listElements().filter {
            it.unwrapKey().get().location() == value
        }.findFirst().orElseThrow()
    }.onFailure {
        throw RuntimeException("Failed to load $value from registry ${this.location()}", it)
    }.getOrThrow()

    fun <T> DataResult<T>.resultOrError() = error().map { it.message() }.orElse(this.result().get().toString())

    context(logger: MeowddingLogger)
    fun <T> Result<T>.debug(message: String): Result<T> = apply {
        this.onFailure {
            logger.debug(message, it)
        }
    }

    fun <T> T.applyCatching(init: T.() -> Unit) = apply {
        runCatching {
            this.init()
        }
    }

    fun Component.asDisplay(): Display = Displays.text(this)
    fun Iterable<Item>.filterNotAir() = this.filterNot { item -> item == Items.AIR }
    fun <T> Iterable<Holder<T>>.unwrap() = this.map { it.value() }
}

enum class SkyOceanModifyIndicator : Translatable {
    PREFIX,
    SUFFIX,
    LORE,
    NOTHING;

    override fun getTranslationKey() = "skyocean.config.main.modify_indicator.${name.lowercase()}"
}

@AutoCollect("LateInitModules")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LateInitModule

@AutoCollect("PreInitModules")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PreInitModule


