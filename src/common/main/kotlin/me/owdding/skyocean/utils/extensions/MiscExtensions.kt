package me.owdding.skyocean.utils.extensions

import java.util.concurrent.atomic.AtomicReference

fun <T> AtomicReference<T>.setUnlessNull(value: T?) = value?.let { set(it) }
