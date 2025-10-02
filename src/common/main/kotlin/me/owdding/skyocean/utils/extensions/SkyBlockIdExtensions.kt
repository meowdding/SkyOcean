package me.owdding.skyocean.utils.extensions

import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

private val reverseNeuStuff = Regex("(\\w)-(\\d{1,2})")

fun SkyBlockId.sanitizeNeu(): SkyBlockId {
    if (this.isItem) {
        val sanitized = this.id.substringAfter(SkyBlockId.DELIMITER).sanitizeNeu()
        return SkyBlockId.item(sanitized)
    }
    return this
}


fun String.sanitizeNeu() = this.replace(reverseNeuStuff, "$1:$2")
