package me.owdding.skyocean.features.dev

import me.owdding.ktcodecs.*
import me.owdding.ktmodules.Module
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.utils.Utils.contains
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.debugToggle
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.parseRomanOrArabic
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick

@Module
object EnchantmentGuideParser {

    val enabled by debugToggle("item/eg_parser", "Parses the max enchantment information from the enchantment guide.")
    private val enchantments = mutableSetOf<ParsedEnchantment>()

    @GenerateCodec
    data class ParsedEnchantment(
        val name: String,
        @FieldName("min_level") @OptionalInt(1) val minLevel: Int = 1,
        @FieldName("max_level") val maxLevel: Int,
        @FieldName("is_ultimate") @OptionalBoolean(false) val isUltimate: Boolean = false,
        val sources: List<EnchantmentSource>,
        @OptionalIfEmpty val conflicts: List<String> = listOf(),
        @OptionalIfEmpty @FieldName("applicable_to") val applicableTo: List<String> = listOf(),
        @OptionalIfEmpty val requirements: List<String> = listOf(),
    )

    @GenerateCodec
    data class EnchantmentSource(
        val name: String,
        val min: Int,
        val max: Int,
    )

    private data class LoreReader(val content: List<String>) {
        private var index = 0

        fun canRead(): Boolean = index < content.size
        fun peek(): String = content[index]
        fun read(): String = content[index++]
        fun skip() = index++
        fun reset() {
            index = 0
        }

        val isTitle: Boolean
            get() = this.canRead() && !this.peek().startsWith(" ")
    }

    @Subscription
    fun itemChangeEvent(event: InventoryChangeEvent) {
        if (!enabled) return
        if (!event.title.endsWith("Enchantments Guide")) return
        if (!event.isInMainPart) return
        if (event.item !in Items.ENCHANTED_BOOK) return

        val lore = LoreReader(event.item.getRawLore())
        while (lore.canRead() && lore.peek() != "") lore.skip()
        lore.skip()

        val map: MutableMap<String, List<String>> = mutableMapOf()

        while (lore.canRead()) {
            if (!lore.isTitle) lore.skip()
            val title = lore.read()
            val list = mutableListOf<String>()
            while (lore.canRead() && lore.peek().startsWith(" - ")) {
                list.add(lore.read())
            }
            map[title] = list
            while (lore.canRead() && lore.peek().isBlank()) lore.skip()
        }

        val name = event.item.cleanName.substringBeforeLast(" ")
        val maxLevel = event.item.cleanName.removePrefix(name).trim().toInt()
        val sources = parseSources(map.getOrDefault("Sources:", map.getOrDefault("Source:", emptyList())))
        val minLevel = sources.minOf { it.min }

        enchantments.add(
            ParsedEnchantment(
                name,
                minLevel,
                maxLevel,
                event.item.getRawLore().contains("You can only have 1 Ultimate"),
                sources,
                map.getOrDefault("Conflicts:", emptyList()).removeListElements(),
                map.getOrDefault("Applied To:", emptyList()).removeListElements(),
                map.getOrDefault("Requirements: ", emptyList()).removeListElements()
            )
        )
    }

    private fun List<String>.removeListElements() = this.map { it.removePrefix(" -").trim() }

    private val sourceRegex = Regex(" - (?<name>.*?) \\((?:(?<min>[IVX]+)-)?(?<max>[IVX]+)\\)")
    private fun parseSources(sources: List<String>): List<EnchantmentSource> = buildList {
        sources.forEach {
            val match = sourceRegex.matchEntire(it) ?: return@forEach
            val name = match.groups["name"]!!.value
            val max = match.groups["max"]!!.value.parseRomanOrArabic()
            val min = match.groups["min"]?.value?.parseRomanOrArabic() ?: max
            add(EnchantmentSource(name, min, max))
        }
    }

    @Subscription
    fun commandRegisterEvent(event: RegisterSkyOceanCommandEvent) {
        event.registerDevWithCallback("serialize_enchantments") {
            Text.of("Click to copy max enchantments!") {
                onClick { McClient.clipboard = enchantments.toJson(CodecHelpers.set()).toPrettyString() }
            }.sendWithPrefix()
        }
    }

}
