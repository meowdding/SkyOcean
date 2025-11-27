package me.owdding.skyocean.utils.codecs

import com.google.common.collect.ImmutableMap
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.Utils.unsafeCast
import java.util.function.Function

class LenientDispatchedMapCodec<K, V>(
    val keyCodec: Codec<K>,
    val valueCodecFunction: (K) -> Codec<out V>,
) : Codec<Map<K, V>> {
    override fun <T : Any> encode(
        input: Map<K, V>,
        ops: DynamicOps<T>,
        prefix: T,
    ): DataResult<T> {
        val builder: RecordBuilder<T> = ops.mapBuilder()

        input.forEach { (key, value) ->
            try {
                builder.add(keyCodec.encodeStart(ops, key), valueCodecFunction(key).encodeStart(ops, value.unsafeCast()))
            } catch (e: Exception) {
                error("Failed to encode field $key -> $value", e)
            }
        }

        return builder.build(prefix)
    }

    companion object : MeowddingLogger by SkyOcean.featureLogger("LenientDispatchedMapCodec")

    override fun <T : Any> decode(
        ops: DynamicOps<T>,
        input: T,
    ): DataResult<Pair<Map<K, V>, T>> {
        return ops.getMap(input).flatMap { map: MapLike<T> ->
            val entries = Object2ObjectArrayMap<K, V>()

            map.entries().forEach { parseEntry(ops, it, entries) }
            val pair: Pair<Map<K, V>, T> = Pair.of(ImmutableMap.copyOf(entries), input)

            DataResult.success(pair)
        }
    }

    private fun <T> parseEntry(
        ops: DynamicOps<T>,
        input: Pair<T, T>,
        entries: MutableMap<K, V>,
    ) {
        try {
            val keyResult: DataResult<K> = keyCodec.parse(ops, input.first)
            val valueResult: DataResult<V> = keyResult.map(this.valueCodecFunction).flatMap { codec -> codec.parse(ops, input.second) }.map(Function.identity())
            val entryResult: DataResult<Pair<K, V>> = keyResult.apply2stable(Pair<K, V>::of, valueResult)
            val entry = entryResult.resultOrPartial()

            if (entry.isPresent) {
                val entry = entry.get()
                val key: K = entry.first
                val value: V = entry.second
                if (entries.putIfAbsent(key, value) != null) {
                    warn("Duplicate key found in lenient dispatched map codec $key -> $value")
                }
            }

            if (entryResult.isError) {
                warn("Failed to parse lenient dispatched map entry (key: ${keyResult.result()}, value: ${valueResult.result()})")
            }
        } catch (e: Exception) {
            error("Caught exception while trying to decode lenient dispatched map ($input)", e)
        }
    }
}
