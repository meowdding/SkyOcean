package me.owdding.skyocean.features.recipe.crafthelper

import me.owdding.ktcodecs.GenerateCodec

@GenerateCodec
data class CraftHelperData(
    var item: String?,
    var amount: Int = 1,
)
