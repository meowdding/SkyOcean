package me.owdding.skyocean.utils.extensions

import earth.terrarium.olympus.client.utils.State

fun State<Boolean>.and(other: Boolean) = this.set(this.get() && other)
fun State<Boolean>.or(other: Boolean) = this.set(this.get() || other)
