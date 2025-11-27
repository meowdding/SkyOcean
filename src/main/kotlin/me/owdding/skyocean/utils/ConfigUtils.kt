package me.owdding.skyocean.utils

import com.teamresourceful.resourcefulconfigkt.api.CachedTransformedEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.NumberBuilder
import kotlin.math.roundToInt

fun EntriesBuilder.transparency(value: Int, builder: NumberBuilder<Int>.() -> Unit = {}): CachedTransformedEntry<Int, Int> {
    return cachedTransform(
        int(value) {
            slider = true
            range = 0..100
            builder.invoke(this)
        },
        { (it / 255.0).toInt() },
        { ((255 / 100.0) * it).roundToInt() },
    )
}
