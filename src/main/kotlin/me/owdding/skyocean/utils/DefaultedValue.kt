package me.owdding.skyocean.utils

import me.owdding.skyocean.utils.debug.DebugRepresentable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

data class DefaultedValue<Type, Value>(var value: Value) : ReadWriteProperty<Type, Value>, DebugRepresentable<Boolean> {
    var isModified: Boolean = false
        private set

    override fun getValue(thisRef: Type, property: KProperty<*>): Value = value
    override fun setValue(thisRef: Type, property: KProperty<*>, value: Value) = setValue(value)
    @JvmName("_set_value_")
    fun setValue(value: Value) {
        this.value = value
        isModified = true
    }
    operator fun invoke() = value

    override fun render(): Boolean = isModified
}
