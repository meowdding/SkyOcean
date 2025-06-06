package me.owdding.skyocean.config

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.Entry
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder
import tech.thatgravyboat.skyblockapi.helpers.McClient

fun <T, B : TypeBuilder> CategoryKt.observable(entry: Entry<T, B>, onChange: () -> Unit) =
    this.observable(entry) { _, _ -> onChange() }

fun CategoryKt.requiresChunkRebuild(entry: Entry<Boolean, *>) = observable(entry) {
    McClient.self.levelRenderer?.allChanged()
}
