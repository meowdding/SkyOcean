package me.owdding.skyocean.data.profile

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.repo.PowderType
import me.owdding.lib.repo.SkillTreeCurrency
import me.owdding.lib.repo.WhisperType
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.unsafeCast
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

    operator fun <T : SkillTreeCurrency> set(type: T, perk: String) {
        val storage = getStorage(type) ?: return
        if (storage[type] == perk) return
        storage[type] = perk
        save()
    }

    fun <T : SkillTreeCurrency> remove(type: T) {
        val storage = getStorage(type) ?: return
        if (storage.remove(type) != null) save()
    }

    private fun <T : SkillTreeCurrency> getStorage(currency: T) = when (currency) {
        is PowderType -> data?.hotm
        is WhisperType -> data?.hotf
        else -> null
    }?.unsafeCast<MutableMap<T, String>>()

    private fun save() = STORAGE.save()
}

@GenerateCodec
data class StoredPerkData(
    val hotm: MutableMap<PowderType, String> = mutableMapOf(),
    val hotf: MutableMap<WhisperType, String> = mutableMapOf(),
)
