package me.owdding.skyocean.features.dev

import me.owdding.ktmodules.Module
import me.owdding.skyocean.api.IngredientParser
import me.owdding.skyocean.features.recipe.SkyOceanItemIngredient
import me.owdding.skyocean.features.recipe.custom.CustomRecipe
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.debugToggle
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.MustBeContainer
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenMouseClickEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Module
object NpcRecipeParser {

    val lastInv: MutableMap<Int, MaybeCustomRecipe> = mutableMapOf()
    val path: Path = Path(System.getProperty("skyocean.recipepath", "config/skyocean/data")).resolve("src/repo/recipes")

    val enable by debugToggle("recipe/npc_parser")
    val amountRegex = Regex(".* (x[\\d,.]+)")

    @OptIn(ExperimentalContracts::class)
    private inline fun <T, R> T.ifEnabled(block: T.() -> R) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        if (!enable) return
        block()
    }

    @Subscription
    @OnlyOnSkyBlock
    @MustBeContainer
    private fun InventoryChangeEvent.inventory() = ifEnabled {
        if (isSkyBlockFiller) return
        val lore = item.getRawLore().joinToString("\n")
        val id = item.getSkyBlockId() ?: return

        val output = IngredientParser.parse(item.hoverName.stripped)?.amount ?: 1

        val costs = lore.substringAfterLast("Cost").trim().split("\n")
            .toList().takeWhile { line -> line.isNotBlank() }.mapNotNull { IngredientParser.parse(it) }

        lastInv[slot.index] = !CustomRecipe(SkyOceanItemIngredient(id, output), costs.toMutableList())
        slot.item.replaceVisually {
            copyFrom(slot.item)
            backgroundItem = Items.RED_STAINED_GLASS_PANE.defaultInstance
        }
    }

    private operator fun CustomRecipe.not() = MaybeCustomRecipe(this, false)

    fun String.getAmount(): Pair<String, Int> = if (this.matches(amountRegex)) {
        val amount = this.replace(amountRegex, "$1")
        this.removeSuffix(amount).trim() to amount.substring(1).toIntValue()
    } else {
        this to 1
    }

    @Subscription
    @OnlyOnSkyBlock
    private fun ContainerCloseEvent.onClose() = ifEnabled {
        var saved = 0
        lastInv.values.filter { it.save }.map { it.recipe }.forEach {
            path.createDirectories()
            val recipe = path.resolve("${it.output?.skyblockId?.replace(":", ".")}.json")
            recipe.writeText(
                it.toJson(SkyOceanCodecs.CustomRecipeCodec.codec()).toPrettyString(),
                Charsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
            )
            saved++
        }
        Text.of("Saved $saved recipes").sendWithPrefix()
        lastInv.clear()
    }

    @Subscription
    @OnlyOnSkyBlock
    private fun ScreenMouseClickEvent.Pre() = ifEnabled {
        val clickedSlot = McScreen.asMenu?.getHoveredSlot() ?: return
        if (clickedSlot.container is Inventory) return
        val data = lastInv[clickedSlot.index] ?: return
        data.save = !data.save
        clickedSlot.item.replaceVisually {
            copyFrom(clickedSlot.item)
            backgroundItem = if (data.save) Items.LIME_STAINED_GLASS_PANE.defaultInstance else Items.RED_STAINED_GLASS_PANE.defaultInstance
        }
        this.cancel()
    }

    data class MaybeCustomRecipe(
        val recipe: CustomRecipe,
        var save: Boolean,
    )
}
