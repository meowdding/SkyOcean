package me.owdding.skyocean.features.mining.scathas

import me.owdding.ktmodules.Module
import me.owdding.skyocean.config.features.mining.ScathaConfig
import me.owdding.skyocean.utils.chat.ChatUtils
import me.owdding.skyocean.utils.chat.ChatUtils.sendWithPrefix
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityInfoLineEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import net.minecraft.sounds.SoundEvents
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color


@Module
object Scathas {

    var worm: SpawnedWorm = SpawnedWorm(null)
    var cooldown: Boolean = false
    val magicFindPetRegex = Regex("^PET DROP! (?<pet>.+) \\(\\+(?<mf>.+)✯ Magic Find\\)$")

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun getWorm(event: EntityInfoLineEvent) {
        if (!ScathaConfig.wormAnnouncer) { return }

        val entity = event.infoLineEntity
        val name = entity.cleanName

        if (name == "[Lv5] Worm 5❤") {
            worm = SpawnedWorm(entity, false)
        }

        if (name == "[Lv10] Scatha 10❤") {
            worm = SpawnedWorm(entity, true)
        }

        if (!worm.isAlive()) { return }

        cooldown = true

        worm.title()
    }

    @Subscription(TickEvent::class)
    @TimePassed("5t")
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onTick() {
        val now = System.currentTimeMillis()
        if (worm.isAlive()) {
            McClient.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP)
        }

        if (cooldown) {
            if ((now - worm.spawnedAt()) >= 29000) {
                cooldown = false
                if (ScathaConfig.wormCooldown) {
                    McClient.setTitle(
                        Text.of {
                            append(ChatUtils.ICON_SPACE_COMPONENT)
                            append("Scatha Cooldown Over") {
                                color = TextColor.GRAY;
                            }
                        },
                        stayTime = 1f
                    )
                    McClient.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 2f)
                }
            }
        }
    }

    val scathaPetDropRegex = Regex("^PET DROP! Scatha(?: \\(\\+(?<mf>\\d+)✯ Magic Find\\))?$")
    @Subscription
    fun onPetDrop(event: ChatReceivedEvent.Pre) {
        if (!scathaPetDropRegex.matches(event.text)) return

        val petComponent = event.component.siblings.firstOrNull {
            it.string.contains("Scatha")
        }
        val rarity = SkyBlockRarity.entries.firstOrNull {
            petComponent?.color == it.color
        }

        if (ScathaConfig.replacePetMessage) {
            event.cancel()

            Text.of(){
                append("PET DROP! ") {
                    this.bold = true
                    this.color = TextColor.GOLD
                }
                append((rarity?.name?.uppercase() + " ") ?: " ") {
                    this.bold = true
                    this.color = rarity?.color ?: TextColor.WHITE
                }
                append(petComponent?.string ?: "PET") {
                    this.color = rarity?.color ?: TextColor.WHITE
                }
            }.sendWithPrefix()
        }

        if (ScathaConfig.petDropTitle) {
            McClient.setTitle(
                Text.of {
                    append(ChatUtils.ICON_SPACE_COMPONENT)
                    append("Scatha Pet!") {
                        color = TextColor.GOLD;
                    }
                },
                Text.of {
                    append(rarity?.name?.uppercase() ?: " ") {
                        color = rarity?.color ?: TextColor.WHITE;
                        this.bold = true
                    }
                },
                stayTime = 3f
            )
            McClient.playSound(SoundEvents.ANVIL_LAND, 1f, 2f)

        }
    }
}
