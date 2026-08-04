package me.owdding.skyocean.utils.nbs

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import java.util.Collections.emptyList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

// This entire NBS implementation was stolen and modified from https://github.com/hollow-cube/mapmaker/blob/main/modules/nbs/src/main/java/net/hollowcube/nbs
interface NBSReader {
    fun read(bytes: ByteArray): NoteBlockSong {
        val buffer = FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))
        return read(buffer)
    }

    fun read(buffer: FriendlyByteBuf): NoteBlockSong

    class Impl : NBSReader {
        override fun read(buffer: FriendlyByteBuf): NoteBlockSong {
            val oldSongLength = NBSTypes.SHORT.decode(buffer)

            val version = if (oldSongLength.toInt() != 0) {
                -1
            } else {
                NBSTypes.BYTE.decode(buffer).toInt()
            }

            val isLegacy = version == -1
            if (!isLegacy && (version !in MIN_VERSION..MAX_VERSION)) {
                throw UnsupportedOperationException("Only OpenNBS versions $MIN_VERSION-$MAX_VERSION are supported!")
            }

            val vanillaInstrumentCount = if (isLegacy) 9 else NBSTypes.BYTE.decode(buffer).toInt()
            val songLengthTicks = if (isLegacy) oldSongLength else (if (version >= 3) NBSTypes.SHORT.decode(buffer) else 0.toShort())
            val layerCount = NBSTypes.SHORT.decode(buffer)

            val songName = NBSTypes.STRING.decode(buffer)
            val songAuthor = NBSTypes.STRING.decode(buffer)
            val songOriginalAuthor = NBSTypes.STRING.decode(buffer)
            val songDescription = NBSTypes.STRING.decode(buffer)

            val tempo = NBSTypes.SHORT.decode(buffer)
            val autoSaving = NBSTypes.BOOL.decode(buffer)
            val autoSavingDuration = NBSTypes.BYTE.decode(buffer)
            val timeSignature = NBSTypes.BYTE.decode(buffer)
            val minutesSpent = NBSTypes.INT.decode(buffer)
            val leftClicks = NBSTypes.INT.decode(buffer)
            val rightClicks = NBSTypes.INT.decode(buffer)
            val noteBlocksAdded = NBSTypes.INT.decode(buffer)
            val noteBlocksRemoved = NBSTypes.INT.decode(buffer)
            val midiSchematicFileName = NBSTypes.STRING.decode(buffer)

            var loop = false
            var maxLoopCount: Byte = 0
            var loopStartTick: Short = 0
            if (version >= 4) {
                loop = NBSTypes.BOOL.decode(buffer)
                maxLoopCount = NBSTypes.BYTE.decode(buffer)
                loopStartTick = NBSTypes.SHORT.decode(buffer)
            }

            val newLayerCount = AtomicInteger()
            val tickData = readTicks(buffer, newLayerCount, isLegacy)
            val layers = readLayers(buffer, max(newLayerCount.get(), layerCount.toInt()), isLegacy)
            val customInstruments = readCustomInstruments(buffer)

            val ticks = tickData.map { it.invoke(layers) }

            return NoteBlockSong(
                vanillaInstrumentCount,
                songLengthTicks,
                layerCount,
                songName,
                songAuthor,
                songOriginalAuthor,
                songDescription,
                tempo,
                autoSaving,
                autoSavingDuration,
                timeSignature,
                minutesSpent,
                leftClicks,
                rightClicks,
                noteBlocksAdded,
                noteBlocksRemoved,
                midiSchematicFileName,
                loop,
                maxLoopCount,
                loopStartTick,
                ticks,
                layers,
                customInstruments,
            )
        }

        private fun readTicks(
            buffer: FriendlyByteBuf,
            layerCount: AtomicInteger,
            isLegacy: Boolean,
        ): List<(List<NoteBlockSong.Layer>) -> NoteBlockSong.Tick> {
            val ticks = ArrayList<(List<NoteBlockSong.Layer>) -> NoteBlockSong.Tick>()
            var t = -1

            while (true) {
                val jumpsToNextTick = NBSTypes.SHORT.decode(buffer).toInt()
                if (jumpsToNextTick == 0) break

                t += jumpsToNextTick
                val instrumentFactories = ArrayList<(List<NoteBlockSong.Layer>) -> NoteBlockSong.Instrument>()

                var layer = -1
                while (true) {
                    val jumpsToNextLayer = NBSTypes.SHORT.decode(buffer).toInt()
                    if (jumpsToNextLayer == 0) break

                    layer += jumpsToNextLayer

                    val instrument = NBSTypes.BYTE.decode(buffer)
                    val noteKey = NBSTypes.BYTE.decode(buffer)
                    val velocity = if (isLegacy) 100.toByte() else NBSTypes.BYTE.decode(buffer)
                    val panning = if (isLegacy) 100.toShort() else NBSTypes.UNSIGNED_BYTE.decode(buffer)
                    val pitch = if (isLegacy) 0.toShort() else NBSTypes.SHORT.decode(buffer)

                    val finalLayer = layer
                    if (finalLayer > layerCount.get()) {
                        layerCount.set(finalLayer + 1)
                    }

                    instrumentFactories.add { layerList ->
                        NoteBlockSong.Instrument(
                            layerList[finalLayer], instrument, noteKey, velocity, panning, pitch,
                        )
                    }
                }

                val finalT: Int = t
                ticks.add { layerList ->
                    NoteBlockSong.Tick(finalT, instrumentFactories.map { it(layerList) })
                }
            }
            return ticks
        }

        private fun readLayers(buffer: FriendlyByteBuf, layerCount: Int, isLegacy: Boolean): MutableList<NoteBlockSong.Layer> {
            if (buffer.readableBytes() == 0) return emptyList()

            val layers = ArrayList<NoteBlockSong.Layer>()
            for (i in 0 until layerCount) {
                val name = NBSTypes.STRING.decode(buffer)
                val locked = !isLegacy && NBSTypes.BOOL.decode(buffer)
                val volume = NBSTypes.BYTE.decode(buffer)
                val stereo = if (isLegacy) 100.toShort() else NBSTypes.UNSIGNED_BYTE.decode(buffer)
                layers.add(NoteBlockSong.Layer(i, name, locked, volume, stereo))
            }
            return layers
        }

        private fun readCustomInstruments(buffer: FriendlyByteBuf): MutableList<NoteBlockSong.CustomInstrument> {
            if (buffer.readableBytes() == 0) return emptyList()
            val customInstrumentsCount = NBSTypes.UNSIGNED_BYTE.decode(buffer).toInt()

            val instruments = ArrayList<NoteBlockSong.CustomInstrument>()
            for (i in 0 until customInstrumentsCount) {
                val name = NBSTypes.STRING.decode(buffer)
                val file = NBSTypes.STRING.decode(buffer)
                val pitch = NBSTypes.BYTE.decode(buffer)
                val pressKey = NBSTypes.BOOL.decode(buffer)
                instruments.add(NoteBlockSong.CustomInstrument(name, file, pitch, pressKey))
            }
            return instruments
        }
    }

    companion object {
        const val MIN_VERSION: Byte = 1
        const val MAX_VERSION: Byte = 5
        private val INSTANCE: NBSReader = Impl()
        fun reader() = INSTANCE
    }
}
