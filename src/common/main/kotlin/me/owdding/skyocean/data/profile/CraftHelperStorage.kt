package me.owdding.skyocean.data.profile

import com.mojang.serialization.Codec
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperData
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.LateInitModule
import me.owdding.skyocean.utils.storage.ProfileStorage

@LateInitModule
object CraftHelperStorage {
    private val storage = ProfileStorage(
        1,
        { CraftHelperData(null) },
        "craft_helper",
    ) { version ->
        when (version) {
            0 -> SkyOceanCodecs.CraftHelperDataCodec.codec().xmap(
                { (item, amount) ->
                    CraftHelperData(
                        item?.id?.let { SkyOceanItemId.Companion.unknownType(it) },
                        amount,
                    )
                },
                { it },
            )

            1 -> SkyOceanCodecs.CraftHelperDataCodec.codec()
            else -> Codec.unit { CraftHelperData(null, 1) }
        }
    }

    fun setSelected(item: SkyOceanItemId?) {
        data?.item = item
        data?.amount = 1
        save()
    }

    fun save() {
        storage.save()
    }

    val data get() = storage.get()
}
