package me.owdding.skyocean.utils.extensions

import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text

inline fun <K, V> Iterable<K>.associateWithNotNull(keySelector: (K) -> V?): Map<K, V> = buildMap {
    for (element in this@associateWithNotNull) put(element, keySelector(element) ?: continue)
}

fun <T> MutableList<T>.truncate(size: Int) {
    val toDrop = this.size - size
    if (toDrop <= 0) return
    this.dropLast(toDrop)
}

fun <T> List<T>.copy() = ArrayList(this)

fun <T, C : Collection<T>> C.nullIfEmpty(): C? = ifEmpty { null }

fun <T> Collection<T>.joinToComponent(separator: String, transform: (T) -> Component) = joinToComponent(Text.of(separator), transform)
fun <T> Collection<T>.joinToComponent(separator: Component, transform: (T) -> Component) = Text.join(this.map(transform), separator = separator.copy())

fun <T> Iterable<T>.indexOfOrNull(predicate: (T) -> Boolean) = indexOfFirst(predicate).takeUnless { it == -1 }
fun <T> Iterable<T>.indexOfOrNull(predicate: T) = indexOf(predicate).takeUnless { it == -1 }

fun <Type> MutableCollection<Type>.addAll(other: Collection<Type>?) = other?.let { this.addAll(it) }

fun <Key, Value> MutableMap<Key, Value>.putAll(other: Map<Key, Value>?) = other?.let { this.putAll(it) }
