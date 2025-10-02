package me.owdding.skyocean.config

import com.teamresourceful.resourcefulconfigkt.api.*
import com.teamresourceful.resourcefulconfigkt.api.builders.CategoryBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.SeparatorBuilder
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toTimeUnit

fun <T> CategoryBuilder.observable(entry: ConfigDelegateProvider<RConfigKtEntry<T>>, onChange: () -> Unit) =
    this.observable(entry) { _, _ -> onChange() }

fun CategoryBuilder.requiresChunkRebuild(entry: ConfigDelegateProvider<RConfigKtEntry<Boolean>>) = observable(entry) {
    runCatching { McClient.self.levelRenderer.allChanged() }
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

fun CategoryBuilder.separator(translation: String) = this.separator { this.translation = translation }

fun ConfigDelegateProvider<RConfigKtEntry<Long>>.duration(unit: DurationUnit): TransformedEntry<Long, Duration> {
    val timeUnit = unit.toTimeUnit()
    return transform({ it.toLong(unit) }) { timeUnit.toMillis(it).milliseconds }
}

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


class CachedValue<T>(private val supplier: () -> T) {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: Any?): T {
        val value = value ?: supplier()
        if (this.value != value) this.value = value
        return value
    }

    fun hasValue() = value != null

    fun invalidate() {
        value = null
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
