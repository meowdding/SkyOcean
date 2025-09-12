package me.owdding.skyocean.utils.extensions

inline fun <K, V> Iterable<K>.associateWithNotNull(keySelector: (K) -> V?): Map<K, V> = buildMap {
    for (element in this@associateWithNotNull) put(element, keySelector(element) ?: continue)
}
