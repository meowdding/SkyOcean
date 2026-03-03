package me.owdding.skyocean.features.mining.scathas

import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.config.features.mining.ScathaConfig
import me.owdding.skyocean.utils.Utils.derpyMaxHp
import me.owdding.skyocean.utils.RemoteStrings
import me.owdding.skyocean.utils.StringGroup.Companion.resolve
import me.owdding.skyocean.utils.chat.ChatUtils
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.decoration.ArmorStand
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyIn
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityAttributesUpdateEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityEquipmentUpdateEvent
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityRemovedEvent
import tech.thatgravyboat.skyblockapi.api.events.hypixel.ServerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.location.ServerDisconnectEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.currentInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.getHelmet
import tech.thatgravyboat.skyblockapi.utils.extentions.getTexture
import tech.thatgravyboat.skyblockapi.utils.extentions.since
import tech.thatgravyboat.skyblockapi.utils.extentions.toReadableTime
import tech.thatgravyboat.skyblockapi.utils.regex.component.match
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Module
object Scathas : MeowddingLogger by SkyOcean.featureLogger() {
    private var lastSpawn: Instant = Instant.DISTANT_PAST

    private var worm: SpawnedWorm? = null
    private var cooldown: Boolean = false

    private val group = RemoteStrings.resolve()

    @Suppress("MaxLineLength")
    private val SKULL_TEXTURE by group.string("ewogICJ0aW1lc3RhbXAiIDogMTYyMDQ0NTc2NDQ1MSwKICAicHJvZmlsZUlkIiA6ICJmNDY0NTcxNDNkMTU0ZmEwOTkxNjBlNGJmNzI3ZGNiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWxhcGFnbzA1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmMDNhZDk2MDkyZjNmNzg5OTAyNDM2NzA5Y2RmNjlkZTZiNzI3YzEyMWIzYzJkYWVmOWZmYTFjY2FlZDE4NmMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==")
    private val spawnWormRegex by group.regex("You hear the sound of something approaching\\.\\.\\.")
    private val scathaPetDropRegex by group.componentRegex("PET DROP! (?<pet>Scatha)(?: \\(\\+(?<mf>\\d+)✯ Magic Find\\))?")


    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onEntityEquipment(event: EntityEquipmentUpdateEvent) {
        if (!ScathaConfig.wormAnnouncer) return
        tryAdd(event.entity)
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onEntityHealthChange(event: EntityAttributesUpdateEvent) {
        if (!ScathaConfig.wormAnnouncer) return
        if (Attributes.MAX_HEALTH !in event.changed) return
        tryAdd(event.entity)
    }

    private fun tryAdd(entity: LivingEntity) {
        if (entity !is ArmorStand) return
        if (entity.getHelmet().getTexture() != SKULL_TEXTURE) return
        debug("Found entity with correct skull texture")
        if (worm?.entity == entity) {
            warn("Worm is already found!")
            return
        }
        val isScatha = when (val hp = entity.derpyMaxHp().roundToInt()) {
            5 -> false
            10 -> true
            else -> {
                error("Worm has invalid hp: $hp")
                return
            }
        }
        debug("worm found is scatha: $isScatha")

        if (lastSpawn.since() > 10.seconds) {
            debug("too long since last spawn: ${lastSpawn.since().toReadableTime()}")
            return
        }
        val worm = SpawnedWorm(entity, isScatha)
        this.worm = worm

        if (!worm.isAlive) return

        worm.title()
    }

    @Subscription(TickEvent::class)
    @TimePassed("5t")
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onTick() {
        if (ScathaConfig.wormAnnouncer && worm?.isAlive == true) {
            McClient.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP)
        }

        if (cooldown) {
            if (lastSpawn.since() < 30.seconds) return
            cooldown = false
            if (ScathaConfig.wormCooldown) {
                McClient.setTitle(
                    Text.join(
                        ChatUtils.ICON_SPACE_COMPONENT,
                        Text.of("Scatha Cooldown Over", TextColor.GRAY),
                    ),
                    stayTime = 1f,
                )
                McClient.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 2f)
            }
        }
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onEntityRemoved(event: EntityRemovedEvent) {
        val worm = worm ?: return
        if (worm.entity == event.entity) this.worm = null
    }

    @Subscription(ServerChangeEvent::class, ServerDisconnectEvent::class)
    fun onServerChange() {
        this.worm = null
        cooldown = false
    }

    @Subscription
    @OnlyIn(SkyBlockIsland.CRYSTAL_HOLLOWS)
    fun onChat(event: ChatReceivedEvent.Pre) {
        if (spawnWormRegex.matches(event.text)) {
            lastSpawn = currentInstant()
            cooldown = true
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
                    Text.join(
                        ChatUtils.ICON_SPACE_COMPONENT,
                        Text.of("Scatha Pet!", TextColor.GOLD),
                    ),
                    Text.of(rarity.name) {
                        color = rarity.color
                        this.bold = true
                    },
                    stayTime = 3f,
                )
                McClient.playSound(SoundEvents.ANVIL_LAND, 1f, 2f)

            }
        }
    }
}
