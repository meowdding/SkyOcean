package me.owdding.skyocean.features.mining.scathas
import me.owdding.skyocean.config.features.mining.ScathaConfig
import me.owdding.skyocean.helpers.glowingColor
import me.owdding.skyocean.helpers.isGlowing
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.world.entity.Entity
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class SpawnedWorm(val entity: Entity?, val scatha: Boolean = false) {
    var birth: Long = 0

    init {
        birth = System.currentTimeMillis()
    }

    fun spawnedAt(): Long {
        if (entity == null) return 0

        return birth

    }

    fun isAlive(): Boolean {
        if (entity == null) return false

        return entity.isAlive
    }

    fun title() {
        if (entity == null) return

        if (!((entity.cleanName.endsWith("10❤") && scatha) || (entity.cleanName.endsWith("5❤") && !scatha))) { return }
        if (scatha) {
            McClient.setTitle(
                Text.of {
                    append(ChatUtils.ICON_SPACE_COMPONENT)
                    append("Scatha") {
                        color = TextColor.GOLD;
                        bold = true;
                    }
                },
                Text.of {
                    append("PRAY TO RNGESUS!") {
                        color = TextColor.RED;
                        bold = true;
                    }
                }, stayTime = 1.5f
            )
        } else {
            McClient.setTitle(
                Text.of {
                    append(ChatUtils.ICON_SPACE_COMPONENT)
                    append("Worm") {
                        color = TextColor.WHITE
                        bold = true
                    }
                },
                Text.of {
                    append("just a worm...") {
                        color = TextColor.GRAY
                    }
                }, stayTime = 1.5f
            )
        }
    }

}
