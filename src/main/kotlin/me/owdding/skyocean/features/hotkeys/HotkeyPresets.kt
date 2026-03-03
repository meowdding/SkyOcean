package me.owdding.skyocean.features.hotkeys

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.lib.events.StartRepoLoadingEvent
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.warn
import me.owdding.skyocean.utils.codecs.CodecHelpers
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription

@Module
object HotkeyPresets : MeowddingLogger by SkyOcean.featureLogger() {

    @GenerateCodec
    data class HotkeyPreset(
        val name: String,
        val description: String,
        val data: String,
    )

    val presets: MutableList<HotkeyPreset> = mutableListOf()

    @Subscription(StartRepoLoadingEvent::class)
    fun startLoading() {
        presets.clear()
    }

    @Subscription(FinishRepoLoadingEvent::class)
    fun load() = runCatching {
        presets.addAll(Utils.loadRemoteRepoData("skyocean/keybind_presets", CodecHelpers.list()))
    }.warn("Failed to load presets!")

}
