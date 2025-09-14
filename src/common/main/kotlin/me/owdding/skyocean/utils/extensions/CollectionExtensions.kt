package me.owdding.skyocean.utils.extensions

import java.util.*

inline fun <K, V> Iterable<K>.associateWithNotNull(keySelector: (K) -> V?): Map<K, V> = buildMap {
    for (element in this@associateWithNotNull) put(element, keySelector(element) ?: continue)
}

fun <T> MutableList<T>.truncate(size: Int) {
    val toDrop = this.size - size
    if (toDrop <= 0) return
    this.dropLast(toDrop)
}

fun <T> List<T>.copy() = LinkedList(this)
