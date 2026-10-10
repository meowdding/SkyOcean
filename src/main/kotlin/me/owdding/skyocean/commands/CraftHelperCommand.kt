package me.owdding.skyocean.commands

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktmodules.Module
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.recipe.ItemLikeIngredient
import me.owdding.skyocean.features.recipe.SimpleRecipeApi
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperManager
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsCycleElement
import me.owdding.skyocean.features.recipe.crafthelper.data.SkyShardsMethod
import me.owdding.skyocean.features.recipe.crafthelper.display.CraftHelperDisplay
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.suggestions.CombinedSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeIdSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeNameSuggestionProvider
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.replaceWith
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.zip.GZIPInputStream
import kotlin.io.encoding.Base64

@Module
object CraftHelperCommand {
    @Subscription
    private fun registerCommands(event: RegisterSkyOceanCommandEvent) {
        fun toId(input: String): ItemLikeIngredient? {
            var amount = 1
            val id = SkyBlockId.fromName(input, dropLast = false) ?: SkyBlockId.unknownType(input) ?: run {
                val splitName = input.substringBeforeLast(" ")
                amount = input.substringAfterLast(" ").toIntOrNull() ?: 1
                SkyBlockId.fromName(splitName) ?: SkyBlockId.unknownType(splitName)
            } ?: return null

            return SkyOceanItemIngredient(id, amount)
        }

        event.command("multi_recipe") {
            "clear" executes CraftHelperStorage::clear

            "add id"(StringArgumentType.greedyString(), CombinedSuggestionProvider(RecipeIdSuggestionProvider, RecipeNameSuggestionProvider)) executes { id ->
                val item = toId(id) ?: return@executes Text.of("Failed to find item with id $id", CatppuccinColors.Mocha.red).sendWithPrefix()
                val inputs = CraftHelperStorage.getOrCreateIngredientRecipe().inputs
                if (inputs.any { it is ItemLikeIngredient && it.id == item.id }) {
                    inputs.replaceWith(
                        inputs.map {
                            if (it is ItemLikeIngredient && it.id == item.id) {
                                it.withAmount(it.amount + item.amount)
                            } else it
                        },
                    )
                } else {
                    inputs.add(item)
                }
                Text.of {
                    append("Added ")
                    if (item.amount > 1) {
                        append(item.amount.toFormattedString(), TextColor.GRAY).append("x ", TextColor.GRAY)
                    }
                    append(item.itemName)
                    append(" to recipe!")
                    color = CatppuccinColors.Mocha.green
                }.sendWithPrefix()
            }
            "remove id"(
                StringArgumentType.greedyString(),
                CombinedSuggestionProvider(RecipeIdSuggestionProvider, RecipeNameSuggestionProvider),
            ) executes { id ->
                val item = toId(id) ?: return@executes Text.of("Failed to find item with id $id", CatppuccinColors.Mocha.red).sendWithPrefix()
                val recipe = CraftHelperStorage.getOrCreateIngredientRecipe()
                val inputs = recipe.inputs
                inputs.replaceWith(
                    inputs.mapNotNull {
                        if (it is ItemLikeIngredient && it.id == item.id) {
                            if (it.amount - item.amount <= 0) null else item.withAmount(it.amount - item.amount)
                        } else it
                    },
                )
                if (inputs.isEmpty()) {
                    CraftHelperStorage.clear()
                    Text.of("Removed item and cleared recipe!", CatppuccinColors.Mocha.green).sendWithPrefix()
                    return@executes
                }
                if (inputs.size == 1) run {
                    val first = inputs.first() as? ItemLikeIngredient ?: return@run
                    CraftHelperStorage.setSelected(first.id)
                    CraftHelperStorage.setAmount(recipe.amount * first.amount)
                }
                Text.of {
                    append("Removed ")
                    if (item.amount > 1) {
                        append(item.amount.toFormattedString(), TextColor.GRAY).append("x ", TextColor.GRAY)
                    }
                    append(item.itemName)
                    append(" from recipe!")
                    color = CatppuccinColors.Mocha.green
                }.sendWithPrefix()
            }
        }

        event.command("recipe") {
            "clear" executes {
                CraftHelperManager.clear()
                text("Cleared current recipe!").sendWithPrefix()
            }

            "skyshards" executes {
                val clipboard = McClient.clipboard
                try {
                    val split = clipboard.split(":")
                    val prefix = split.first()
                    val suffix = split.getOrNull(1)
                    if (!prefix.startsWith("<SkyOceanRecipe>(V", true) || suffix == null) {
                        text("Your clipboard does not contain any known tree format!") {
                            this.color = OceanColors.WARNING
                        }.sendWithPrefix()
                        return@executes
                    }
                    val base = Base64.decode(suffix.trim())
                    val data = GZIPInputStream(base.inputStream()).use { it.readBytes() }.decodeToString()
                        .readJson<JsonObject>().toData(SkyOceanCodecs.SkyShardsMethodCodec.codec())

                    data?.let {
                        val list = mutableListOf<SkyShardsMethod>()
                        it.visitElements(list::add)

                        val containsCycle = list.any { it is SkyShardsCycleElement }
                        CraftHelperStorage.setSkyShards(it)
                        if (containsCycle) {
                            text("The imported tree contains a cycle, these are currently not supported in skyocean! The tree might not look complete!") {
                                this.color = OceanColors.WARNING
                            }.sendWithPrefix()
                        } else {
                            text("Set current recipe to SkyShards Tree for ") {
                                append("${it.quantity.toFormattedString()}x ") { color = TextColor.GREEN }
                                append(it.shard.toItem().hoverName)
                                append("!")
                            }.sendWithPrefix()
                        }
                    } ?: run {
                        text("Failed to read SkyShards data from clipboard!") { this.color = OceanColors.WARNING }.sendWithPrefix()
                    }
                } catch (e: Exception) {
                    text("Failed to read SkyShards data from clipboard!") { this.color = OceanColors.WARNING }.sendWithPrefix()
                    CraftHelperDisplay.error("Failed to decode SkyShards tree!", e)
                }
            }

            "amount"(IntegerArgumentType.integer(1)) executes { amount ->
                val craftAmount = CraftHelperStorage.selectedItem?.let(SimpleRecipeApi::getBestRecipe)?.output?.amount ?: 1
                CraftHelperStorage.setAmount(Utils.nextUp(amount, craftAmount))
                CraftHelperStorage.save()
                text("Set current recipe amount to ") {
                    append("$amount") { color = TextColor.GREEN }
                    append("!").sendWithPrefix()
                }
            }

            "recipe"(
                StringArgumentType.greedyString(),
                CombinedSuggestionProvider(RecipeIdSuggestionProvider, RecipeNameSuggestionProvider),
            ) executes { input ->
                val item = toId(input) ?: return@executes CraftHelperStorage.setSelected(null)
                CraftHelperStorage.setSelected(item.id)
                val craftAmount = item.let(SimpleRecipeApi::getBestRecipe)?.output?.amount ?: 1
                CraftHelperStorage.setAmount(Utils.nextUp(item.amount, craftAmount))
                CraftHelperStorage.save()
                text("Set current recipe to ") {
                    append("${CraftHelperStorage.selectedAmount}x ") { color = TextColor.GREEN }
                    append(CraftHelperStorage.selectedItem?.toItem()?.let(ItemStack::getHoverName) ?: !"unknown")
                    append("!")
                }.sendWithPrefix()
            }
        }
    }

}
