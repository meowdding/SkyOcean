package me.owdding.skyocean.features.mining.scathas

import me.owdding.skyocean.utils.SoundUtils
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class SpawnedWorm(val entity: Entity, val scatha: Boolean = false) {
    val isAlive: Boolean get() = entity.isAlive

    fun title() {
        if (scatha) {
            McClient.setTitle(
                Text.join(
                    ChatUtils.ICON_SPACE_COMPONENT,
                    Text.of("Scatha") {
                        color = TextColor.GOLD
                        bold = true
                    },
                ),
                Text.of("PRAY TO RNGESUS!") {
                    color = TextColor.RED
                    bold = true
                },
                stayTime = 1.5f,
            )
            SoundUtils.playRepeated(SoundEvents.NOTE_BLOCK_PLING.value(), 5)
        } else {
            McClient.setTitle(
                Text.join(
                    ChatUtils.ICON_SPACE_COMPONENT,
                    Text.of("Worm") {
                        color = TextColor.WHITE
                        bold = true
                    },
                ),
                Text.of("just a worm...", TextColor.GRAY),
                stayTime = 1.5f,
            )
        }
    }

}
