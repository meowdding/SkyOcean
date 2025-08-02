package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.api.SkyOceanItemId

@GenerateCodec
data class CraftHelperData(
    var item: SkyOceanItemId?,
    var amount: Int = 1,
)
