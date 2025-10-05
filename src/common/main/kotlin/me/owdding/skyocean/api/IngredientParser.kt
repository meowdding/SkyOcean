package me.owdding.skyocean.api

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.features.recipe.CurrencyIngredient
import me.owdding.skyocean.features.recipe.CurrencyType
import me.owdding.skyocean.features.recipe.Ingredient
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object IngredientParser {
    private data class CurrencyInformation(val name: String, val formatting: CurrencyFormatting = CurrencyFormatting.CURRENCY_LIKE)
    enum class CurrencyFormatting {
        CURRENCY_LIKE,
        ITEM_LIKE,
    }

    private val currencies = mapOf<CurrencyType, CurrencyInformation>(
        CurrencyType.COIN to CurrencyInformation("Coins"),
        CurrencyType.BIT to CurrencyInformation("Bits"),
        CurrencyType.COPPER to CurrencyInformation("Copper"),
        CurrencyType.FOSSIL_DUST to CurrencyInformation("Fossil Dust"),
        CurrencyType.BRONZE_MEDAL to CurrencyInformation("Bronze medal", CurrencyFormatting.ITEM_LIKE),
        CurrencyType.SILVER_MEDAL to CurrencyInformation("Silver medal", CurrencyFormatting.ITEM_LIKE),
        CurrencyType.GOLD_MEDAL to CurrencyInformation("Gold medal", CurrencyFormatting.ITEM_LIKE),
        CurrencyType.MOTE to CurrencyInformation("Motes"),
        CurrencyType.NORTH_STAR to CurrencyInformation("North Stars"),
        CurrencyType.PELT to CurrencyInformation("Pelts"),
        CurrencyType.GEM to CurrencyInformation("Gems"),
        CurrencyType.CHOCOLATE to CurrencyInformation("Chocolate"),
    )
    val currencyRegex = Regex(
        buildString {
            append("(?<amount>[\\d,.]+) (?<type>")
            append(currencies.values.filter { it.formatting == CurrencyFormatting.CURRENCY_LIKE }.joinToString("|") { it.name })
            append(")")
        }.also { SkyOcean.debug(it) },
    )
    val itemLikeCurrencyRegex = Regex(
        buildString {
            append("(?<type>")
            append(currencies.values.filter { it.formatting == CurrencyFormatting.ITEM_LIKE }.joinToString("|") { it.name })
            append(")(?: x(?<amount>[\\d,.]+))?")
        }.also { SkyOcean.debug(it) },
    )

    val itemRegex = Regex("(.*?)(?: x([\\d,]+))?")

    fun parseCurrency(ingredient: String, regex: Regex): CurrencyIngredient? {
        val currencyName = ingredient.replace(currencyRegex, "\${type}")
        val currency = currencies.entries.find { it.value.name == currencyName }?.key ?: run {
            Text.of("Unable to parse currency information for line $ingredient ($currencyName)") { this.color = TextColor.RED }.sendWithPrefix()
            return null
        }
        val price = ingredient.replace(currencyRegex, "\${amount}").substringBeforeLast(".").toIntValue()
        return CurrencyIngredient(price, currency)
    }

    fun parse(ingredient: String): Ingredient? {
        val ingredient = ingredient.trim()

        if (ingredient.matches(currencyRegex)) return parseCurrency(ingredient, currencyRegex)
        if (ingredient.matches(itemLikeCurrencyRegex)) return parseCurrency(ingredient, itemLikeCurrencyRegex)

        val item = ingredient.replace(itemRegex, "$1")
        val amount = ingredient.replace(itemRegex, "$2")
            .replace(item, "").toIntValue().coerceAtLeast(1)

        val id = SkyBlockId.fromName(item) ?: run {
            Text.of("Unable to parse item information for line $ingredient") { this.color = TextColor.RED }.send()
            return null
        }

        return SkyOceanItemIngredient(id, amount)
    }

}
