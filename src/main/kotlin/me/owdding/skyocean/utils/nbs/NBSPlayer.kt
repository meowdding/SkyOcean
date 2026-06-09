package me.owdding.skyocean.utils.nbs

import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.helpers.McClient
import kotlin.concurrent.thread
import kotlin.math.pow

class NBSPlayer(private val song: NoteBlockSong) {

    companion object {

        private val soundLookup: Array<SoundEvent> = arrayOf(
            SoundEvents.NOTE_BLOCK_HARP.value(),
            SoundEvents.NOTE_BLOCK_BASS.value(),
            SoundEvents.NOTE_BLOCK_BASEDRUM.value(),
            SoundEvents.NOTE_BLOCK_SNARE.value(),
            SoundEvents.NOTE_BLOCK_HAT.value(),
            SoundEvents.NOTE_BLOCK_GUITAR.value(),
            SoundEvents.NOTE_BLOCK_FLUTE.value(),
            SoundEvents.NOTE_BLOCK_BELL.value(),
            SoundEvents.NOTE_BLOCK_CHIME.value(),
            SoundEvents.NOTE_BLOCK_XYLOPHONE.value(),
            SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(),
            SoundEvents.NOTE_BLOCK_COW_BELL.value(),
            SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),
            SoundEvents.NOTE_BLOCK_BIT.value(),
            SoundEvents.NOTE_BLOCK_BANJO.value(),
            SoundEvents.NOTE_BLOCK_PLING.value(),
        )
    }

    private val ticks: Array<NoteBlockSong.Tick?> = arrayOfNulls(song.songLengthTicks + 1)

    var task: Thread? = null
        private set

    private var isPlaying = false
    private var position = 0
    private var loopedAmount = 0
    private var isPaused = false

    init {
        for (tick in song.ticks) {
            this.ticks[tick.tickTime] = tick
        }
    }

    fun start() {
        if (this.task != null) {
            resume()
        } else {
            this.isPlaying = true
            this.task = thread(start = true, isDaemon = true) { run() }
        }
    }

    private fun run() {
        while (this.isPlaying && this.position <= this.song.songLengthTicks) {
            if (!this.isPaused) tick()

            try {
                Thread.sleep((1000L / (this.song.tempo / 100f)).toLong())
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    private fun tick() {
        val tick = this.ticks[this.position++] ?: return

        for (instrument in tick) {
            val instrumentId = instrument.instrument.toInt()
            if (instrumentId !in soundLookup.indices) continue

            val soundEvent = soundLookup[instrumentId]
            val volume = (instrument.layer.volume * instrument.noteBlockVelocity) / 100f
            val pitch = getPitch(instrument)

            McClient.runNextTick {
                McClient.self.soundManager.play(SimpleSoundInstance.forUI(soundEvent, pitch, volume))
            }
        }

        loop()
    }

    private fun loop() {
        if (this.position > this.song.songLengthTicks) {
            if (!this.song.loop) return
            if (this.loopedAmount >= this.song.maxLoopCount.toInt() && this.song.maxLoopCount.toInt() != 0) return
            if (this.position >= this.song.songLengthTicks.toInt()) {
                this.position = song.loopStartTick.toInt()
                this.loopedAmount++
            }
        }
    }

    private fun getPitch(instrument: NoteBlockSong.Instrument): Float {
        val key = when {
            instrument.noteBlockKey < 33 -> instrument.noteBlockKey - 9
            instrument.noteBlockKey > 57 -> instrument.noteBlockKey - 57
            else -> instrument.noteBlockKey - 33
        }

        return (0.5 * 2.0.pow(key / 12.0)).toFloat()
    }

    fun stop() {
        if (this.task != null) this.isPlaying = false
        this.task?.interrupt()
        this.task = null
        this.position = 0
    }

    fun restart() {
        assertStarted()
        this.position = 0
    }

    fun pause() {
        assertStarted()
        this.isPaused = true
    }

    fun resume() {
        assertStarted()
        this.isPaused = false
    }

    private fun assertStarted() {
        if (this.task == null) throw RuntimeException("Not started yet!")
    }
}
