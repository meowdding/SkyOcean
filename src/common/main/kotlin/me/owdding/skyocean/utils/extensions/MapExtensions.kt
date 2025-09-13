package me.owdding.skyocean.utils.extensions

fun <K, V> MutableMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean): MutableMap<K, V> = also { entries.removeIf(predicate) }
