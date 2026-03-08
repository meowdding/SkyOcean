package me.owdding.skyocean.features.item.custom.ui.standard

import earth.terrarium.olympus.client.components.buttons.Button
import me.owdding.skyocean.features.item.custom.CustomItems
import me.owdding.skyocean.features.item.custom.CustomItems.getKey
import me.owdding.skyocean.features.item.custom.CustomItems.getOrTryCreateCustomData
import me.owdding.skyocean.utils.Utils.text
import me.owdding.skyocean.utils.chat.CatppuccinColors
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import me.owdding.skyocean.utils.debugToggle
import me.owdding.skyocean.utils.tags.ItemTagKey
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object StandardCustomizationUi {

    val debug by debugToggle("customization/ui/debug")

    val buttons: MutableList<Button> = mutableListOf()
    var anyUpdated: Boolean = false
        set(value) {
            buttons.forEach { it.active = value }
            field = value
        }

    fun buttonClick() = McClient.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1f)

    fun open(item: ItemStack) {
        anyUpdated = false
        buttons.clear()
        McClient.setScreenAsync { ItemCustomizationModal(item, McScreen.self) }
    }

    fun reset(copy: ItemStack) {
        buttons.clear()
        CustomItems.staticMap.remove(copy.getKey())
    }

    fun save(item: ItemStack, copy: ItemStack) {
        buttons.clear()
        val edited = CustomItems.staticMap[copy.getKey()] ?: return
        if (item !in ItemTagKey.DYEABLE) {
            text("Item might not support coloring!") {
                this.color = CatppuccinColors.Mocha.red
            }.sendWithPrefix()
        }
        item.getOrTryCreateCustomData()?.data?.apply {
            clear()
            putAll(edited.data)
        }
        CustomItems.save()
    }

}
