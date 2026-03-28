package me.owdding.skyocean.utils.nbs

import org.jetbrains.annotations.Nullable

data class NoteBlockSong(
    val vanillaInstrumentCount: Int,
    val songLengthTicks: Short,
    val layerCount: Short,
    val name: String?,
    val author: String?,
    val originalAuthor: String?,
    val description: String?,
    val tempo: Short,
    val autoSaving: Boolean,
    val autoSavingDuration: Byte,
    val timeSignature: Byte,
    val minutesSpent: Int,
    val leftClicks: Int,
    val rightClicks: Int,
    val noteBlocksAdded: Int,
    val noteBlocksRemoved: Int,
    val midiSchematicFileName: String?,
    val loop: Boolean,
    val maxLoopCount: Byte,
    val loopStartTick: Short,
    val ticks: List<Tick>,
    val layers: List<Layer>,
    val customInstruments: List<CustomInstrument>
) {

    data class Instrument(
        val layer: Layer,
        val instrument: Byte,
        val noteBlockKey: Byte,
        val noteBlockVelocity: Byte, // volume from 0-100 in percent
        val noteBlockPanning: Short,
        val noteBlockPitch: Short
    )

    data class Tick(
        val tickTime: Int, // The index this tick is at
        val instruments: List<Instrument>
    ) : Iterable<Instrument> {
        override fun iterator(): Iterator<Instrument> = instruments.iterator()
    }

    data class Layer(
        val index: Int,
        val name: String?,
        val locked: Boolean,
        val volume: Byte, // Percentage 0-100
        val stereo: Short // 0-200 (200 = two blocks left, 100 = center, 0 = two blocks right)
    )

    data class CustomInstrument(
        val name: String?,
        val soundFile: String?,
        val soundKey: Byte,
        val pressPianoKey: Boolean
    )


    /**
     * IMPORTANT! This class is not in the nbs spec, we encode every data in the data map into the description as JSON. The link also gets loaded from there.
     */
    data class Data(
        val name: String?,
        val author: String?,
        val originalAuthor: String?,
        val description: String?,
        @param:Nullable val link: String?,
        val data: Map<String, String>
    )
}
