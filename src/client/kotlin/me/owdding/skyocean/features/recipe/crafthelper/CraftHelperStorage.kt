package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.storage.ProfileStorage

@LateInitModule
object CraftHelperStorage {
    private val storage = ProfileStorage(
        0,
        { CraftHelperData(null) },
        "craft_helper",
        {
            SkyOceanCodecs.CraftHelperDataCodec.codec()
        },
    )

    fun save() {
        storage.save()
    }

    val data get() = storage.get()
}
