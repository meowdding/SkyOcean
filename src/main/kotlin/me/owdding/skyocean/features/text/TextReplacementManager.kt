package me.owdding.skyocean.features.text

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktcodecs.OptionalBoolean
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.extensions.indexOfOrNull
import me.owdding.skyocean.utils.storage.DataStorage
import net.minecraft.util.Util
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import java.util.HashSet
import java.util.UUID
import java.util.function.Function

@Module
object TextReplacementManager {

    val defaultCategory = TextReplacementCategory(
        Util.NIL_UUID,
        "Uncategorized",
    )

    val categories: MutableSet<TextReplacementCategory> get() = storage.get().categories

    @IncludedCodec(named = "text_replacement_set")
    val textReplacementSet: Codec<HashSet<TextReplacement>> = CodecHelpers.mutableSet<TextReplacement>().xmap(::HashSet, Function.identity())

    @GenerateCodec
    @NamedCodec("TextReplacementData")
    data class StoredData(
        val categories: MutableSet<TextReplacementCategory>,
        @NamedCodec("text_replacement_set") val replacements: HashSet<TextReplacement>,
    )

    private val storage: DataStorage<StoredData> = DataStorage(
        { StoredData(mutableSetOf(), HashSet()) },
        "text_replacements",
        Codec.withAlternative(
            SkyOceanCodecs.TextReplacementDataCodec.codec(),
            textReplacementSet.xmap({ StoredData(mutableSetOf(), it) }, { it.replacements }),
        ),
    )

    val replacements = mutableListOf<TextReplacement>()

    init {
        storage.get().replacements.forEach(::registerInternal)
    }

    @Subscription
    fun registerCommand(event: RegisterSkyOceanCommandEvent) {
        event.registerWithCallback("text_replacements") {
            McClient.setScreenAsync { TextReplacementScreen }
        }
    }

    fun replacements() = storage.get().replacements

    fun registerInternal(replacement: TextReplacement) {
        val insertionSpot = replacements.indexOfOrNull { it.priority < replacement.priority }
        if (insertionSpot == null) {
            replacements.add(replacement)
        } else {
            replacements.add(insertionSpot, replacement)
        }
    }

    fun register(replacement: TextReplacement) {
        registerInternal(replacement)
        this.storage.edit {
            replacements.add(replacement)
        }
    }

    fun unregister(replacement: TextReplacement) {
        replacements.remove(replacement)

        this.storage.edit {
            replacements.removeIf { it === replacement }
        }
    }

    fun createCategory(name: String, madeBy: String): TextReplacementCategory {
        val category = TextReplacementCategory(UUID.randomUUID(), name, madeBy)
        this.storage.edit {
            categories.add(category)
        }
        return category
    }

    fun deleteCategory(category: TextReplacementCategory) {
        this.storage.edit {
            replacements.removeAll { it.category == category.identifier }
            categories.removeIf { it.identifier == category.identifier }
        }
    }

    fun save() {
        storage.save()
    }
}

@GenerateCodec
data class TextReplacementCategory(
    val identifier: UUID,
    var name: String,
    var username: String = McPlayer.name,
    @OptionalBoolean(true) var enabled: Boolean = true,
) {

    fun isDefault(): Boolean = this === TextReplacementManager.defaultCategory

    fun getReplacementsInCategory(): Collection<TextReplacement> = if (isDefault()) {
        TextReplacementManager.replacements().filter { it.category == null }
    } else {
        TextReplacementManager.replacements().filter { it.category == identifier }
    }
}

interface DisableReplacements
