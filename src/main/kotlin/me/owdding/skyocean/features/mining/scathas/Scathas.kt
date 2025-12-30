package me.owdding.skyocean.features.mining.scathas

import kotlin.time.Duration.Companion.seconds
import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.ScathaConfig
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.append
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityInfoLineEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.regex.component.ComponentRegex
import tech.thatgravyboat.skyblockapi.utils.regex.component.match
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object Scathas {

    private var worm: SpawnedWorm? = null
    private var cooldown: Boolean = false
    private val scathaPetDropRegex = ComponentRegex("PET DROP! (?<pet>Scatha)(?: \\(\\+(?<mf>\\d+)✯ Magic Find\\))?")

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun getWorm(event: EntityInfoLineEvent) {
        if (!ScathaConfig.wormAnnouncer) return

        val entity = event.infoLineEntity
        val name = entity.cleanName

        val isScatha = when (name) {
            "[Lv5] Worm 5❤" -> false
            "[Lv10] Scatha 10❤" -> true
            else -> return
        }

        val worm = SpawnedWorm(entity, isScatha)
        this.worm = worm

        if (!worm.isAlive()) return

        cooldown = true

        worm.title()
    }

    @Subscription(TickEvent::class)
    @TimePassed("5t")
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onTick() {
        val worm = worm ?: return
        if (worm.isAlive()) {
            McClient.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP)
        }

        if (cooldown) {
            if ((worm.spawnedAt().since() > 29.seconds)) {
                cooldown = false
                if (ScathaConfig.wormCooldown) {
                    McClient.setTitle(
                        ChatUtils.ICON_SPACE_COMPONENT.copy().append("Scatha Cooldown Over") {
                            color = TextColor.GRAY
                        },
                        stayTime = 1f
                    )
                    McClient.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 2f)
                }
            }
        }
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onPetDrop(event: ChatReceivedEvent.Post) {
        scathaPetDropRegex.match(event.component) { matcher ->
            val pet = matcher["pet"] ?: return@match
            val mf: Component? = matcher["mf"]
            val rarity = SkyBlockRarity.entries.find { it.color == pet.color } ?: return@match
            if (ScathaConfig.replacePetMessage) {
                event.component = Text.of {
                    append("PET DROP! ") {
                        this.bold = true
                        this.color = TextColor.GOLD
                    }
                    append((rarity.name)) {
                        this.bold = true
                        this.color = rarity.color
                    }
                    append(CommonText.SPACE)
                    append(pet)

                    if (mf != null) {
                        append {
                            append("(+")
                            append(mf)
                            append("% ✯ Magic Find)")
                            color = TextColor.AQUA
                        }
                    }
                }
            }

            if (ScathaConfig.petDropTitle) {
                McClient.setTitle(
                    ChatUtils.ICON_SPACE_COMPONENT.copy().append("Scatha Pet!") {
                        color = TextColor.GOLD
                    },
                    Text.of(rarity.name) {
                        color = rarity.color
                        this.bold = true
                    },
                    stayTime = 3f
                )
                McClient.playSound(SoundEvents.ANVIL_LAND, 1f, 2f)

            }
        }
    }
}
