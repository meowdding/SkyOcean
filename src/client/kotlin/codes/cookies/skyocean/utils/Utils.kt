package codes.cookies.skyocean.utils

import codes.cookies.skyocean.SkyOcean
import kotlinx.coroutines.runBlocking
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.width
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.split
import java.nio.file.Files

// TODO: surely better name maybe?
object Utils {
    inline fun <reified T : Any> loadFromRepo(file: String) = runBlocking {
        try {
            SkyOcean.SELF.findPath("repo/$file.json").orElseThrow()?.let(Files::readString)?.readJson<T>() ?: return@runBlocking null
        } catch (e: Exception) {
            SkyOcean.error("Failed to load $file from repo", e)
            null
        }
    }

    infix fun Int.exclusiveInclusive(other: Int) = (this + 1)..other
    infix fun Int.exclusiveExclusive(other: Int) = (this + 1)..(other - 1)

    operator fun Item.contains(stack: ItemStack): Boolean = stack.item == this

    // TODO: m-lib or sbapi idfk
    fun Component.split(separator: String, maxLength: Int): List<Component> {
        val separatorComponent = Text.of(separator)
        val splits = this.split(separator)
        val components = mutableListOf<Component>()
        var current = mutableListOf<Component>()
        var currentLength = 0
        for (split in splits) {
            val splitLength = split.width
            if (currentLength + splitLength > maxLength) {
                components.add(Text.join(*current.toTypedArray(), separator = separatorComponent))
                current.clear()
                currentLength = 0
            }
            current.add(split)
            currentLength += splitLength
        }

        if (current.isNotEmpty()) {
            components.add(Text.join(*current.toTypedArray(), separator = separatorComponent))
        }

        return components
    }

    fun String.split(separator: String, maxLength: Int): List<String> {
        val splits = this.split(separator)
        val components = mutableListOf<String>()
        var current = mutableListOf<String>()
        var currentLength = 0
        for (split in splits) {
            val splitLength = Text.of(split).width
            if (currentLength + splitLength > maxLength) {
                components.add(current.joinToString(separator))
                current.clear()
                currentLength = 0
            }
            current.add(split)
            currentLength += splitLength
        }

        if (current.isNotEmpty()) {
            components.add(current.joinToString(separator))
        }

        return components
    }
}
