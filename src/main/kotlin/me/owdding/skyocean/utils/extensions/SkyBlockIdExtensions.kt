package me.owdding.skyocean.utils.extensions

import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

fun SkyBlockId.toIngredient(amount: Int = 1) = SkyOceanItemIngredient(this, amount)
