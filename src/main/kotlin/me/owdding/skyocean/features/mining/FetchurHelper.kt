package me.owdding.skyocean.features.mining

import com.mojang.brigadier.arguments.StringArgumentType
import me.owdding.ktcodecs.Compact
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.MiningConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.events.RegisterSkyOceanCommandEvent
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.crafthelper.data.FetchurCraftHelperRecipe
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.chat.OceanColors
import me.owdding.skyocean.utils.codecs.CodecHelpers
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.profile.ProfileChangeEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland.DWARVEN_MINES
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findGroup
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.onClick

@Module
object FetchurHelper {
    private val group = RemoteStrings.resolve()
    private val correctItem by group.string("thanks thats probably what i needed")
    private val regex by group.regex("^\\[NPC] Fetchur: (?<message>.+)")

    private val fetchurItems = Utils.loadRepoData("mining/fetchur", CodecHelpers.list<FetchurItem>())
    private var fetchurItem: FetchurItem? = null

    private val craftHelperButton by lazy {
        text {
            append(" [", TextColor.YELLOW)
            append("Craft Helper", TextColor.GOLD)
            append("]", TextColor.YELLOW)

            onClick {
                val item = fetchurItem ?: return@onClick
                CraftHelperStorage.set(
                    FetchurCraftHelperRecipe(
                        item.items.mapTo(mutableListOf()) { SkyOceanItemIngredient(it) },
                        item.amount,
                    ),
                )
                Text.of("Added ") {
                    color = OceanColors.BASE_TEXT
                    append(item.itemName, TextColor.BLUE)
                    append(" to CraftHelper.")
                }.sendWithPrefix()
            }
        }
    }

    @Subscription
    @OnlyIn(DWARVEN_MINES)
    private fun onChatReceived(event: ChatReceivedEvent.Pre) {
        val message = regex.findGroup(event.text, "message") ?: return
        if (message.equals(correctItem, true)) {
            reset()
            return
        }
        if (!MiningConfig.fetchurHelper) return
        fetchurThing(message)
    }

    private fun fetchurThing(dialogue: String) {
        val item = fetchurItems.find { it.message.equals(dialogue, true) } ?: return
        fetchurItem = item
        McClient.runNextTick {
            text {
                append("Fetchur wants: ", OceanColors.BASE_TEXT)
                append("${item.amount}x ", TextColor.BLUE)
                append(item.itemName, OceanColors.HIGHLIGHT)
                append(craftHelperButton)
            }.sendWithPrefix()
        }
    }

    private fun reset() {
        fetchurItem = null
    }

    @Subscription
    private fun onCommand(event: RegisterSkyOceanCommandEvent) {
        event.command("dev testfetchur") {
            "string"(StringArgumentType.greedyString()) executes { string ->
                fetchurThing(string)
            }
        }
    }

    @Subscription(ProfileChangeEvent::class)
    private fun onProfileChange() = reset()

    @GenerateCodec
    data class FetchurItem(
        val message: String,
        @Compact val items: List<SkyBlockId>,
        val amount: Int,
        val override: String?,
    ) {
        val itemName: String by lazy {
            override ?: run {
                require(items.size == 1) { "Fetchur items must have exactly one item if no override is provided" }
                items.single().toItem().cleanName
            }
        }
    }
}
