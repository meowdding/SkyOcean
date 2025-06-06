package me.owdding.skyocean.utils

import com.teamresourceful.resourcefulconfigkt.api.TransformedEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.NumberBuilder

fun EntriesBuilder.transparency(value: Int, builder: NumberBuilder<Int>.() -> Unit = {}): TransformedEntry<Int, NumberBuilder<Int>, Int> {
    return transform(
        int(value) {
            slider = true
            range = 0..100
            builder.invoke(this)
        },
        { (it / 255.0).toInt() },
        { ((255 / 100.0) * it).toInt() },
    )
}
