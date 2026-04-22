package me.owdding.skyocean.utils

import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.ClientLevelChangeEvent
import me.owdding.skyocean.utils.LevelBoundValueHolder.register
import me.owdding.skyocean.utils.Utils.unsafeCast
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Instant

@Suppress("ClassName")
private data object UNINITIALIZED_VALUE

@Module
data object LevelBoundValueHolder {
    val list: MutableSet<WeakReference<LevelBoundValue<*>>> = ConcurrentHashMap.newKeySet()

    fun LevelBoundValue<*>.register() {
        list.add(WeakReference(this))
    }

    @Subscription
    context(_: ClientLevelChangeEvent)
    fun onLevelChange() {
        val iterator = list.iterator()

        while (iterator.hasNext()) {
            val reference = iterator.next()
            reference.get()?.invalidate() ?: iterator.remove()
        }
    }
}

data class MutableLevelBoundValue<Type>(val delegate: LevelBoundValue<Type>, val setter: (Type) -> Unit = {}) {
    operator fun <This> getValue(thisRef: This?, property: Any?) = delegate.getValue()
    operator fun <This> setValue(thisRef: This?, property: Any?, value: Type) {
        setter(value)
        delegate.setValue(value)
    }
}

fun <Type> LevelBoundValue<Type>.withSetter(setter: (Type) -> Unit = {}) = MutableLevelBoundValue(this, setter)

fun <Type> levelBound(timeToLive: Duration = Duration.INFINITE, supplier: () -> Type) = LevelBoundValue(timeToLive, supplier)

class LevelBoundValue<Type>(private val timeToLive: Duration = Duration.INFINITE, private val supplier: () -> Type) {

    init {
        register()
    }

    private var value: Any? = UNINITIALIZED_VALUE
    var lastUpdated: Instant = Instant.DISTANT_PAST

    operator fun <This> getValue(thisRef: This?, property: Any?) = this.getValue()

    fun setValue(type: Type) {
        this.value = type
        this.lastUpdated = currentInstant()
    }

    fun getValue(): Type = synchronized(this) {
        if (!this.hasValue()) {
            this.setValue(this.supplier())
        }
        if (this.value === UNINITIALIZED_VALUE) throw ClassCastException("Failed to initialize value!")
        return this.value.unsafeCast()
    }

    fun hasValue() = this.value !== UNINITIALIZED_VALUE && this.lastUpdated.since() < this.timeToLive

    fun invalidate() {
        this.value = UNINITIALIZED_VALUE
    }
}
