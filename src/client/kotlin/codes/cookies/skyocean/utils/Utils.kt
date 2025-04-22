package codes.cookies.skyocean.utils

import codes.cookies.skyocean.SkyOcean
import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import java.nio.file.Files
import kotlin.time.Duration
import kotlin.time.DurationUnit

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

    fun Duration.formatReadableTime(biggestUnit: DurationUnit = DurationUnit.DAYS, maxUnits: Int = 2): String {
        val units = listOf(
            DurationUnit.DAYS to this.inWholeDays,
            DurationUnit.HOURS to this.inWholeHours % 24,
            DurationUnit.MINUTES to this.inWholeMinutes % 60,
            DurationUnit.SECONDS to this.inWholeSeconds % 60,
        )

        val unitNames = mapOf(
            DurationUnit.DAYS to "d",
            DurationUnit.HOURS to "h",
            DurationUnit.MINUTES to "min",
            DurationUnit.SECONDS to "s",
        )

        val filteredUnits = units.dropWhile { it.first != biggestUnit }
            .filter { it.second > 0 }
            .take(maxUnits)

        return filteredUnits.joinToString(", ") { (unit, value) ->
            "$value${unitNames[unit]}"
        }.ifEmpty { "0 seconds" }
    }

    infix fun Int.exclusiveInclusive(other: Int) = (this + 1) .. other
    infix fun Int.exclusiveExclusive(other: Int) = (this + 1) .. (other - 1)

}
