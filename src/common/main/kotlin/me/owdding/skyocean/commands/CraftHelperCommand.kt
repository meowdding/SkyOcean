package me.owdding.skyocean.commands

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktmodules.Module
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.recipe.crafthelper.CraftHelperManager
import me.owdding.skyocean.features.recipe.crafthelper.SkyShardsCycleElement
import me.owdding.skyocean.features.recipe.crafthelper.SkyShardsMethod
import me.owdding.skyocean.features.recipe.crafthelper.display.CraftHelperDisplay
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.OceanColors
import me.owdding.skyocean.utils.Utils.not
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.suggestions.CombinedSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeIdSuggestionProvider
import me.owdding.skyocean.utils.suggestions.RecipeNameSuggestionProvider
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.zip.GZIPInputStream
import kotlin.io.encoding.Base64

@Module
object CraftHelperCommand {

    @Subscription
    fun registerCommands(event: RegisterSkyOceanCommandEvent) {
        event.register("recipe") {
            thenCallback("clear") {
                CraftHelperManager.clear()
                text("Cleared current recipe!").sendWithPrefix()
            }
            then("amount", IntegerArgumentType.integer()) {
                callback {
                    val amount = this.getArgument("amount", Int::class.java)
                    if (amount <= 0) {
                        text("Amount must be greater than 0!").withColor(TextColor.RED).sendWithPrefix()
                        return@callback
                    }
                    CraftHelperStorage.setAmount(amount)
                    CraftHelperStorage.save()
                    text("Set current recipe amount to ") {
                        append("$amount") { color = TextColor.GREEN }
                        append("!").sendWithPrefix()
                    }
                }
            }
            thenCallback("skyshards") {
                val clipboard = McClient.clipboard
                try {
                    val split = clipboard.split(":")
                    val prefix = split.first()
                    val suffix = split.getOrNull(1)
                    if (!prefix.equals("<SkyOceanRecipe>(V1)", true) || suffix == null) {
                        text("Your clipboard does not contain any known tree format!") {
                            this.color = OceanColors.WARNING
                        }.sendWithPrefix()
                        return@thenCallback
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
            then("recipe", StringArgumentType.greedyString(), CombinedSuggestionProvider(RecipeIdSuggestionProvider, RecipeNameSuggestionProvider)) {
                callback {
                    val input = this.getArgument("recipe", String::class.java)
                    var amount = 1
                    val item = SkyOceanItemId.fromName(input, dropLast = false) ?: SkyOceanItemId.unknownType(input) ?: run {
                        val splitName = input.substringBeforeLast(" ")
                        amount = input.substringAfterLast(" ").toIntOrNull() ?: 1
                        SkyOceanItemId.fromName(splitName) ?: SkyOceanItemId.unknownType(splitName)
                    }
                    CraftHelperStorage.setSelected(item)
                    CraftHelperStorage.setAmount(amount)
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

}
