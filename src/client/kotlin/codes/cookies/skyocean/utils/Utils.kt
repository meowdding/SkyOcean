package codes.cookies.skyocean.utils

import codes.cookies.skyocean.SkyOcean
import kotlinx.coroutines.runBlocking
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.text.Text
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

    fun MutableComponent.append(text: String, init: MutableComponent.() -> Unit): MutableComponent = this.append(Text.of(text, init))
}
