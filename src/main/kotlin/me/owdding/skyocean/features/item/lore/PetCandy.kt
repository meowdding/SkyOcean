package me.owdding.skyocean.features.item.lore

import me.owdding.skyocean.config.features.misc.MiscConfig
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@ItemModifier
object PetCandy : AbstractItemModifier() {
    override val displayName: Component get() = +"skyocean.config.misc.showHiddenPetCandy"
    override val isEnabled: Boolean get() = MiscConfig.showHiddenPetCandy

    override fun appliesTo(itemStack: ItemStack) = itemStack.getSkyBlockId() == "PET" && itemStack.getRawLore().contains("MAX LEVEL")

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
        val candy = item.getData(DataTypes.PET_DATA)?.candyUsed?.takeUnless { it == 0 } ?: return@withMerger Result.unmodified

        addBeforeNext({ it.stripped == "MAX LEVEL" }) {
            add(
                Text.of {
                    color = TextColor.GREEN
                    append("(")
                    append("$candy")
                    append("/10) Pet Candy Used")
                },
            )
            add(CommonText.EMPTY)
        }
        Result.modified
    }
}
