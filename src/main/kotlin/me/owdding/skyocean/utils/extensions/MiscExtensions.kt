package me.owdding.skyocean.utils.extensions

import me.owdding.lib.utils.MeowddingLogger
import java.util.concurrent.atomic.AtomicReference

fun <T> AtomicReference<T>.setUnlessNull(value: T?) = value?.let { set(it) }
fun Int?.orElse(other: Int = 1) = this ?: other

fun <T> MeowddingLogger.runCatchingFlat(task: String, block: () -> T) = runCatching {
    block()
}.onFailure {
    this.error(task, it)
}

fun <T> MeowddingLogger.runCatching(task: String, block: () -> T): T? = runCatching {
    block()
}.onFailure {
    this.error(task, it)
}.getOrNull()
