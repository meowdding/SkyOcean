package me.owdding.skyocean.features.textures

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.utils.Utils.visitSiblings
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityInfoLineEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font
import java.util.concurrent.ConcurrentHashMap

@Module
object MobIcons {

    val MOB_ICONS = id("mob_icons")
    val MOB_ICONS_SHORT = id("mob_icons/short")

    private val MOB_TYPES = Regex(KnownMobIcon.entries.joinToString("|") { it.icon })

    @Subscription
    @OnlyOnSkyBlock
    fun attachmentModifyEvent(event: EntityInfoLineEvent) {
        val stripped = event.literalComponent
        if (!stripped.matches(Regex("(?:﴾ )?\\[Lv.*"))) return
        if (!stripped.contains(MOB_TYPES)) return
        event.component.visitSiblings {
            val stripped = it.stripped.trim()
            if (stripped.matches(MOB_TYPES)) {
                KnownMobIcon.getByIcon(stripped)?.name ?: return@visitSiblings
                (it as? MutableComponent)?.font = MOB_ICONS_SHORT
            }
        }
    }

}

enum class KnownMobIcon(val icon: String, short: String? = null) {
    UNDEAD('༕', "UND"),
    SKELETAL("🦴", "SKEL"),
    ENDER('⊙', "END"),
    ATHROPOD('Ж', "ATHR"),
    HUMANOID('✰', "HUM"),
    INFERNAL('♨', "INF"),
    CUBIC('⚂', "CUB"),
    FROZEN('☃', "FRO"),
    SPOOKY('☽', "BOO"),
    MYTHOLOGICAL('✿', "MYTH"),
    WITHER('☠', "WITH"),
    SUBTERRANEAN('⛏', "SUB"),
    AQUATIC('⚓', "AQUA"),
    PEST('ൠ'),
    ANIMAL('☮', "ANI"),
    MAGMATIC('♆', "MAGM"),
    ELUSIVE('♣', "ELUS"),
    CONSTRUCT('⚙', "CONST"),
    ARCANE('♃', "ARC"),
    SHIELDED('⛨', "SHIE"),
    AIRBORNE('✈', "AIR"),
    GLACIAL('❆', "GLAC"),
    WOODLAND('⸙', "WOOD")
    ;

    val short: String = short ?: name

    constructor(icon: Char, short: String? = null) : this(icon.toString(), short)

    companion object {
        private val cache: MutableMap<String, KnownMobIcon?> = ConcurrentHashMap()

        fun getByIcon(icon: String) = cache.getOrPut(icon) {
            KnownMobIcon.entries.find { it.icon == icon }
        }
    }
}
