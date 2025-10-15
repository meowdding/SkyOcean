package me.owdding.skyocean.utils.extensions

import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

private val reverseNeuStuff = Regex("(\\w)-(\\d{1,2})")

fun SkyBlockId.sanitizeNeu(): SkyBlockId {
    if (this.isItem) {
        val sanitized = this.id.substringAfter(SkyBlockId.DELIMITER).sanitizeNeu()
        return SkyBlockId.item(sanitized)
    }
    return this
}


fun SkyBlockId.toIngredient(amount: Int = 1) = SkyOceanItemIngredient(this, amount)

fun String.sanitizeNeu() = this.replace(reverseNeuStuff, "$1:$2")
