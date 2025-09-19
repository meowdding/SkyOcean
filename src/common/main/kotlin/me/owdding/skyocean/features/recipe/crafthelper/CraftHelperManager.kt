package me.owdding.skyocean.features.recipe.crafthelper

import com.mojang.blaze3d.platform.InputConstants
import me.owdding.ktmodules.Module
import me.owdding.lib.compat.REIRuntimeCompatability
import me.owdding.skyocean.api.SkyOceanItemId
import me.owdding.skyocean.config.SkyOceanKeybind
import me.owdding.skyocean.config.features.misc.CraftHelperConfig
import me.owdding.skyocean.data.profile.CraftHelperStorage
import me.owdding.skyocean.data.profile.CraftHelperStorage.setSelected
import me.owdding.skyocean.features.item.sources.ItemSources
import me.owdding.skyocean.features.recipe.crafthelper.eval.ItemTracker
import me.owdding.skyocean.features.recipe.crafthelper.views.SimpleRecipeView
import me.owdding.skyocean.utils.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.Utils.text
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.screen.ScreenKeyReleasedEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getHoveredSlot
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object CraftHelperManager {
    var lastData: CraftHelperRecipe? = null
    var hasBeenNotified = false
    private val keybind = SkyOceanKeybind("skyocean.keybind.crafthelper", InputConstants.KEY_V)


    fun clear() {
        CraftHelperStorage.clear()
        CraftHelperStorage.save()
    }

    @Subscription(TickEvent::class)
    @TimePassed("5t")
    fun onTick() {
        if (lastData != CraftHelperStorage.data) {
            this.lastData = CraftHelperStorage.data
            hasBeenNotified = false
        }
        if (hasBeenNotified) return

        val (tree) = CraftHelperStorage.data?.resolve({}, ::clear) ?: return
        SimpleRecipeView {
            if (!CraftHelperConfig.doneMessage) return@SimpleRecipeView
            if (it.path != "root") return@SimpleRecipeView
            if (!it.childrenDone) return@SimpleRecipeView
            hasBeenNotified = true
            text("You have all materials to craft your selected craft helper tree!").sendWithPrefix()
        }.visit(tree, ItemTracker(ItemSources.craftHelperSources))
    }


    @Subscription
    fun onKeybind(event: ScreenKeyReleasedEvent.Pre) {
        if (!keybind.matches(event)) return

        val reiHovered = REIRuntimeCompatability.getReiHoveredItemStack()
        val mcScreenHovered = McScreen.asMenu?.getHoveredSlot()?.item?.takeUnless { it.isEmpty }
        val item = mcScreenHovered ?: reiHovered ?: return

        setSelected(SkyOceanItemId.fromItem(item))
        McScreen.self?.let { it.resize(McClient.self, it.width, it.height) }

        Text.of("Set selected Crafthelper item to ") {
            append(item.cleanName) {
                this.color = TextColor.GOLD
                this.bold = true
            }
        }.sendWithPrefix()
    }
}
