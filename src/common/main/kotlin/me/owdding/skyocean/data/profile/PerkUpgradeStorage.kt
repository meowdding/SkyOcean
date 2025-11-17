package me.owdding.skyocean.data.profile

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.repo.PowderType
import me.owdding.lib.repo.WhisperType
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.storage.ProfileStorage

object PerkUpgradeStorage {

    private val STORAGE = ProfileStorage(
        defaultData = ::StoredPerkData,
        fileName = "perk_upgrade",
        codec = { SkyOceanCodecs.getCodec<StoredPerkData>() },
    )

    private inline val data get() = STORAGE.get()

    val hotm: Map<PowderType, String>
        get() = data?.hotm.orEmpty()

    val hotf: Map<WhisperType, String>
        get() = data?.hotf.orEmpty()

    operator fun set(type: PowderType, perk: String) {
        val data = data ?: return
        if (data.hotm[type] == perk) return
        data.hotm[type] = perk
        save()
    }

    fun remove(type: PowderType) {
        val data = data ?: return
        if (data.hotm.remove(type) != null) save()
    }

    operator fun set(type: WhisperType, perk: String) {
        val data = data ?: return
        if (data.hotf[type] == perk) return
        data.hotf[type] = perk
    }

    fun remove(type: WhisperType) {
        val data = data ?: return
        if (data.hotf.remove(type) != null) save()
    }

    private fun save() = STORAGE.save()

}

@GenerateCodec
data class StoredPerkData(
    val hotm: MutableMap<PowderType, String> = mutableMapOf(),
    val hotf: MutableMap<WhisperType, String> = mutableMapOf(),
)
