package me.owdding.skyocean.features.dungeons.gambling

import me.owdding.skyocean.config.features.dungeons.DungeonsConfig
import me.owdding.skyocean.features.dungeons.gambling.DungeonGambling.allowedDungeonGamblingChests
import me.owdding.skyocean.features.dungeons.gambling.chest.DungeonChestType
import me.owdding.skyocean.features.item.modifier.AbstractItemModifier
import me.owdding.skyocean.features.item.modifier.ItemModifier
import me.owdding.skyocean.utils.Utils.add
import me.owdding.skyocean.utils.Utils.skipRemaining
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.obfuscated

@ItemModifier
object CroesusImpl : AbstractItemModifier() {
    private val enabled get() = DungeonsConfig.gamblingScreenEnabled && DungeonsConfig.gamblingInCroesus
    val croesusLoreToFloor = mapOf(
        "Catacombs - Floor I" to DungeonFloor.F1,
        "Catacombs - Floor II" to DungeonFloor.F2,
        "Catacombs - Floor III" to DungeonFloor.F3,
        "Catacombs - Floor IV" to DungeonFloor.F4,
        "Catacombs - Floor V" to DungeonFloor.F5,
        "Catacombs - Floor VI" to DungeonFloor.F6,
        "Catacombs - Floor VII" to DungeonFloor.F7,
        "Master Mode Catacombs - Floor I" to DungeonFloor.M1,
        "Master Mode Catacombs - Floor II" to DungeonFloor.M2,
        "Master Mode Catacombs - Floor III" to DungeonFloor.M3,
        "Master Mode Catacombs - Floor IV" to DungeonFloor.M4,
        "Master Mode Catacombs - Floor V" to DungeonFloor.M5,
        "Master Mode Catacombs - Floor VI" to DungeonFloor.M6,
        "Master Mode Catacombs - Floor VII" to DungeonFloor.M7,
    )

    override val displayName: Component get() = Text.of("Croesus Gambling Item Hider")
    override val isEnabled: Boolean get() = enabled

    override fun appliesTo(itemStack: ItemStack) =
        itemStack.`is`(Items.PLAYER_HEAD) && DungeonChestType.getByNameStartsWith(itemStack.cleanName) in allowedDungeonGamblingChests

    override fun appliesToScreen(screen: Screen) = screen.title.stripped.let {
        it.startsWith("Catacombs - ") || it.startsWith("Master Mode Catacombs - ")
    }

    override fun modifyTooltip(item: ItemStack, list: MutableList<Component>, previousResult: Result?) = withMerger(list) {
        addUntilAfter { it.stripped == "Contents" }
        skipRemaining()

        add {
            color = TextColor.GRAY
            append("Hidden by SkyOcean")
        }

        add {
            color = TextColor.GRAY
            append("\"")
            append(Text.translatable("skyocean.config.dungeons.gambling.enabled"))
            append("\" feature.")
        }

        space()
        add("Cost") {
            color = TextColor.GRAY
        }
        add {
            color = TextColor.GOLD
            append("xxxxxxx") {
                obfuscated = true
            }
            append(" Coins")
        }
        space()
        add("Click to open!") {
            color = TextColor.YELLOW
        }

        Result.modified
    }
}
