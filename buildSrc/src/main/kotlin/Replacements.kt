/*
Parts of this file and the idea where taken from NobaAddons https://codeberg.org/nobaboy/NobaAddons
Please also check out their project!

File: https://codeberg.org/nobaboy/NobaAddons/src/commit/cf1e7aa0cd45802eae3d231a9958c9844e1373f3/buildSrc/src/main/kotlin/builddata.kt
 */
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import org.gradle.api.Project
import java.io.BufferedReader
import java.io.File

@Serializable
sealed interface Replacement

@Serializable
@SerialName("regex")
data class RegexReplacement(
    val condition: String,
    val regex: String,
    val to: String,
    val reverseRegex: String,
    val reverse: String,
    val named: Boolean = false,
) : Replacement

@Serializable
@SerialName("string")
data class StringReplacement(
    val condition: String,
    val from: String,
    val to: String,
    val named: Boolean = false,
) : Replacement

private val toml = Toml.Default

@JvmInline
@Serializable
value class Replacements(val replacements: Map<String, Replacement>) {
    companion object {
        fun read(project: Project): Replacements = project.rootProject.file("gradle/replacements.toml").readToml()
    }
}

private inline fun <reified T> File.readToml(): T = BufferedReader(reader()).use {
    toml.decodeFromNativeReader(it)
}
