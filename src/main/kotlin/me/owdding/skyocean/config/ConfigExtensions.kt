package me.owdding.skyocean.config

import com.teamresourceful.resourcefulconfigkt.api.*
import com.teamresourceful.resourcefulconfigkt.api.builders.ButtonBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.CategoryBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.ColorBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.DraggableBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.KeyBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.NumberBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.SelectBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.SeparatorBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.StringBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder
import me.owdding.skyocean.utils.Utils.unsafeCast
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toTimeUnit

fun <T> CategoryBuilder.observable(entry: ConfigDelegateProvider<RConfigKtEntry<T>>, onChange: () -> Unit) = this.observable(entry) { _, _ -> onChange() }

fun CategoryBuilder.requiresChunkRebuild(entry: ConfigDelegateProvider<RConfigKtEntry<Boolean>>) = observable(entry) {
    runCatching {
        //~ if >= 26.2 'levelRenderer' -> 'levelExtractor'
        McClient.self.levelExtractor.allChanged()
    }
}

var SeparatorBuilder.translation: String
    get() = ""
    set(value) {
        this.title = value
        this.description = "$value.desc"
    }

fun CategoryBuilder.category(category: CategoryKt, init: CategoryKt.() -> Unit) {
    category(category)
    category.init()
}

fun CategoryBuilder.categories(vararg entries: CategoryKt) {
    entries.forEach(::category)
}

fun CategoryBuilder.separator(translation: String) = this.separator { this.translation = translation }

fun ConfigDelegateProvider<RConfigKtEntry<Long>>.duration(unit: DurationUnit): CachedTransformedEntry<Long, Duration> {
    val timeUnit = unit.toTimeUnit()
    return cachedTransform({ it.toLong(unit) }) { timeUnit.toMillis(it).milliseconds }
}

fun <T, R> ConfigDelegateProvider<RConfigKtEntry<T>>.cachedTransform(from: (R) -> T, to: (T) -> R) = CachedTransformedEntry(this, from, to)

fun <T, R> ConfigDelegateProvider<RConfigKtEntry<T>>.transform(from: (R) -> T, to: (T) -> R) = TransformedEntry(this, from, to)

fun <T> ConfigDelegateProvider<RConfigKtEntry<T>>.observable(onChange: (T, T) -> Unit) = ObservableEntry(this, onChange)

@Suppress("UnusedReceiverParameter")
fun <T> CategoryBuilder.defaultEnabledMessage(
    entry: ConfigDelegateProvider<RConfigKtEntry<T>>,
    messageProvider: () -> Component,
    id: String,
    predicate: () -> Boolean = { true },
) = DefaultEnabledMessageEntry(entry, messageProvider, id, predicate)


class DefaultEnabledMessageEntry<T>(
    private val entry: ConfigDelegateProvider<RConfigKtEntry<T>>,
    private val messageProvider: () -> Component,
    private val id: String,
    private val predicate: () -> Boolean,
) : ConfigDelegateProvider<RConfigKtEntry<T>> {
    override operator fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): RConfigKtEntry<T> {
        val property = entry.provideDelegate(entries, prop)
        return DefaultEnabledMessageEntryDelegate(property.parent, messageProvider, id, predicate)
    }
}

class DefaultEnabledMessageEntryDelegate<T> internal constructor(
    override val parent: RConfigKtEntry<T>,
    val messageProvider: () -> Component,
    val id: String,
    val predicate: () -> Boolean,
) : RConfigKtEntry<T> by parent {
    override fun getValue(thisRef: Any?, property: Any?): T {
        if (DefaultEnabledMessageHelper.needsSend(id) && predicate.invoke()) {
            messageProvider().sendWithPrefix()
            DefaultEnabledMessageHelper.markSend(id)
        }
        return parent.getValue(thisRef, property)
    }
}

@Suppress("ClassName")
private object UNINITIALIZED_VALUE

class CachedValue<Type>(private val timeToLive: Duration = Duration.INFINITE, private val supplier: () -> Type) {
    private var value: Any? = UNINITIALIZED_VALUE
    var lastUpdated: Instant = Instant.DISTANT_PAST

    operator fun getValue(thisRef: Any?, property: Any?) = getValue()

    fun getValue(): Type {
        if (!hasValue()) {
            this.value = supplier()
            lastUpdated = currentInstant()
        }
        if (value === UNINITIALIZED_VALUE) throw ClassCastException("Failed to initialize value!")
        return value.unsafeCast()
    }

    fun hasValue() = value !== UNINITIALIZED_VALUE && lastUpdated.since() < timeToLive

    fun invalidate() {
        value = UNINITIALIZED_VALUE
    }
}

fun <T> CategoryBuilder.invalidProperty(
    entry: ConfigDelegateProvider<RConfigKtEntry<T>>,
    property: CachedValue<*>,
): ConfigDelegateProvider<RConfigKtEntry<T>> {
    return this.observable(entry) {
        property.invalidate()
    }
}

abstract class DelegatingConfig(val entryBuilder: EntriesBuilder) {
    abstract val translationBase: String

    fun makeTranslation(id: String) = listOf(translationBase, id).filter { it.isNotEmpty() }.joinToString(".").replace("-", ".")

    fun TypeBuilder.makeTranslations() {
        this.translation = makeTranslation(id)
    }

    fun <Type : TypeBuilder> wrap(builder: (Type) -> Unit): (Type) -> Unit = {
        it.makeTranslations()
        builder(it)
    }

    inner class DelegateWrap<Type>(val entry:  ConfigDelegateProvider<RConfigKtEntry<Type>>) : ConfigDelegateProvider<RConfigKtEntry<Type>> {
        operator fun <Any> provideDelegate(owner: Any, property: KProperty<*>) = entry.provideDelegate(entryBuilder, property)
        override fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): RConfigKtEntry<Type> = entry.provideDelegate(entries, prop)
    }

    open fun <Type>  ConfigDelegateProvider<RConfigKtEntry<Type>>.wrap() = DelegateWrap(this)

    fun byte(value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = entryBuilder.byte(value, wrap(builder)).wrap()
    fun byte(id: String, value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = entryBuilder.byte(id, value, wrap(builder)).wrap()


    fun bytes(vararg value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = entryBuilder.bytes(value = value, wrap(builder)).wrap()
    fun bytes(id: String, vararg value: Byte, builder: NumberBuilder<Byte>.() -> Unit = {}) = entryBuilder.bytes(id, value = value, wrap(builder)).wrap()


    fun short(value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = entryBuilder.short(value, wrap(builder)).wrap()
    fun short(id: String, value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = entryBuilder.short(id, value, wrap(builder)).wrap()


    fun shorts(vararg value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = entryBuilder.shorts(value = value, wrap(builder)).wrap()
    fun shorts(id: String, vararg value: Short, builder: NumberBuilder<Short>.() -> Unit = {}) = entryBuilder.shorts(id, value = value, wrap(builder)).wrap()


    fun int(value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = entryBuilder.int(value = value, wrap(builder)).wrap()
    fun int(id: String, value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = entryBuilder.int(id, value = value, wrap(builder)).wrap()


    fun ints(vararg value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = entryBuilder.ints(value = value, wrap(builder)).wrap()
    fun ints(id: String, vararg value: Int, builder: NumberBuilder<Int>.() -> Unit = {}) = entryBuilder.ints(id, value = value, wrap(builder)).wrap()


    fun long(value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = entryBuilder.long(value = value, wrap(builder)).wrap()
    fun long(id: String, value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = entryBuilder.long(id, value = value, wrap(builder)).wrap()


    fun longs(vararg value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = entryBuilder.longs(value = value, wrap(builder)).wrap()
    fun longs(id: String, vararg value: Long, builder: NumberBuilder<Long>.() -> Unit = {}) = entryBuilder.longs(id, value = value, wrap(builder)).wrap()


    fun float(value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = entryBuilder.float(value = value, wrap(builder)).wrap()
    fun float(id: String, value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = entryBuilder.float(id, value = value, wrap(builder)).wrap()


    fun floats(vararg value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = entryBuilder.floats(value = value, wrap(builder)).wrap()
    fun floats(id: String, vararg value: Float, builder: NumberBuilder<Float>.() -> Unit = {}) = entryBuilder.floats(id, value = value, wrap(builder)).wrap()


    fun double(value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = entryBuilder.double(value = value, wrap(builder)).wrap()
    fun double(id: String, value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = entryBuilder.double(id, value = value, wrap(builder)).wrap()


    fun doubles(vararg value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) = entryBuilder.doubles(value = value, wrap(builder)).wrap()
    fun doubles(id: String, vararg value: Double, builder: NumberBuilder<Double>.() -> Unit = {}) =
        entryBuilder.doubles(id, value = value, wrap(builder)).wrap()


    fun boolean(value: Boolean, builder: TypeBuilder.() -> Unit = {}) = entryBuilder.boolean(value = value, wrap(builder)).wrap()
    fun boolean(id: String? = null, value: Boolean, builder: TypeBuilder.() -> Unit = {}) = entryBuilder.boolean(id, value = value, wrap(builder)).wrap()


    fun booleans(vararg value: Boolean, builder: TypeBuilder.() -> Unit = {}) = entryBuilder.booleans(value = value, wrap(builder)).wrap()
    fun booleans(id: String, vararg value: Boolean, builder: TypeBuilder.() -> Unit = {}) = entryBuilder.booleans(id, value = value, wrap(builder)).wrap()


    fun string(value: String, builder: StringBuilder.() -> Unit = {}) = entryBuilder.string(value = value, wrap(builder)).wrap()
    fun string(id: String, value: String, builder: StringBuilder.() -> Unit = {}) = entryBuilder.string(id, value = value, wrap(builder)).wrap()

    // Very hacky but sadly the varargs if its nullable makes it weird

    fun strings(vararg value: String, builder: StringBuilder.() -> Unit = {}) = entryBuilder.strings(value = value, wrap(builder)).wrap()
    fun strings(id: String, vararg value: String, builder: StringBuilder.() -> Unit = {}) = entryBuilder.stringsWithId(id, value = value, wrap(builder)).wrap()


    fun <T : Enum<T>> enum(value: T, builder: TypeBuilder.() -> Unit = {}) = entryBuilder.enum(value = value, wrap(builder)).wrap()
    fun <T : Enum<T>> enum(id: String, value: T, builder: TypeBuilder.() -> Unit = {}) = entryBuilder.enum(id, value = value, wrap(builder)).wrap()


    fun <T : Enum<T>> enums(vararg value: T, builder: TypeBuilder.() -> Unit = {}) = entryBuilder.enums(value = value, wrap(builder)).wrap()
    fun <T : Enum<T>> enums(id: String, vararg value: T, builder: TypeBuilder.() -> Unit = {}) = entryBuilder.enums(id, value = value, wrap(builder)).wrap()

    // special

    fun key(value: Int, builder: KeyBuilder.() -> Unit = {}) = entryBuilder.key(value = value, wrap(builder)).wrap()
    fun key(id: String, value: Int, builder: KeyBuilder.() -> Unit = {}) = entryBuilder.key(id, value = value, wrap(builder)).wrap()


    fun color(value: Int, builder: ColorBuilder.() -> Unit = {}) = entryBuilder.color(value = value, wrap(builder)).wrap()
    fun color(id: String, value: Int, builder: ColorBuilder.() -> Unit = {}) = entryBuilder.color(id, value = value, wrap(builder)).wrap()


    fun <T : Enum<T>> select(vararg value: T, builder: SelectBuilder<T>.() -> Unit = {}) = entryBuilder.select(value = value, wrap(builder)).wrap()
    fun <T : Enum<T>> select(id: String, vararg value: T, builder: SelectBuilder<T>.() -> Unit = {}) =
        entryBuilder.select(id, value = value, wrap(builder)).wrap()


    fun <T : Enum<T>> draggable(vararg value: T, builder: DraggableBuilder<T>.() -> Unit = {}) = entryBuilder.draggable(value = value, wrap(builder)).wrap()
    fun <T : Enum<T>> draggable(id: String, vararg value: T, builder: DraggableBuilder<T>.() -> Unit = {}) =
        entryBuilder.draggable(id, value = value, wrap(builder)).wrap()

    fun button(id: String, buttonBuilder: ButtonBuilder.() -> Unit) = entryBuilder.button {
        this.description = makeTranslation(id) + ".desc"
        this.title = makeTranslation(id)
        this.text = makeTranslation(id) + ".text"
        buttonBuilder()
    }

    fun <T> observable(entry: Entry<T, *>, onChange: (T, T) -> Unit) = ObservableEntry(entry, onChange)
    fun <T> observable(entry: ConfigDelegateProvider<RConfigKtEntry<T>>, onChange: (T, T) -> Unit) = ObservableEntry(entry, onChange)
    fun <T, R> transform(entry: Entry<T, *>, from: (R) -> T, to: (T) -> R) = TransformedEntry(entry, from, to)
    fun <T, R> transform(entry: ConfigDelegateProvider<RConfigKtEntry<T>>, from: (R) -> T, to: (T) -> R) = TransformedEntry(entry, from, to)
    fun <T, R> cachedTransform(entry: Entry<T, *>, from: (R) -> T, to: (T) -> R) = CachedTransformedEntry(entry, from, to)
    fun <T, R> cachedTransform(entry: ConfigDelegateProvider<RConfigKtEntry<T>>, from: (R) -> T, to: (T) -> R) = CachedTransformedEntry(entry, from, to)

}
