package codes.cookies.skyocean.config

import com.google.gson.JsonObject
import org.intellij.lang.annotations.Language
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.util.function.UnaryOperator

fun renameOption(@Language("JSONPath") pathOrigin: String, @Language("JSONPath") moveTo: String) = patch {
    val path = it.getPath(pathOrigin)
    val parent = moveTo.substringBeforeLast(".", "")
    val name = moveTo.substringAfterLast(".")
    it.getPath(parent, true)?.asJsonObject?.add(name, path)
}

private fun patch(patcher: (JsonObject) -> Unit): UnaryOperator<JsonObject> = UnaryOperator {
    patcher(it)
    it
}
