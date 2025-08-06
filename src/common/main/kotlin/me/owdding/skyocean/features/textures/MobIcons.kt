package me.owdding.skyocean.features.textures

import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.config.features.misc.MobIconsConfig
import me.owdding.skyocean.utils.Utils.visitSiblings
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityInfoLineEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font
import java.util.concurrent.ConcurrentHashMap

@Module
object MobIcons {

    val MOB_ICONS = id("mob_icons")
    val MOB_ICONS_SHORT = id("mob_icons/short")


    enum class DisplayType(val font: ResourceLocation) {
        NORMAL(MOB_ICONS),
        SHORT(MOB_ICONS_SHORT),
        ;
    }

    private val MOB_TYPES = Regex(KnownMobIcon.entries.joinToString("|") { it.icon })

    @Subscription
    @OnlyOnSkyBlock
    fun attachmentModifyEvent(event: EntityInfoLineEvent) {
        if (!MobIconsConfig.enabled) return
        val stripped = event.literalComponent
        if (!stripped.matches(Regex("(?:﴾ )?\\[Lv.*"))) return
        if (!stripped.contains(MOB_TYPES)) return
        event.component.visitSiblings { it ->
            val stripped = it.stripped.trim()
            if (stripped.matches(MOB_TYPES)) {
                val text = (it as? MutableComponent) ?: return@visitSiblings
                val icon = KnownMobIcon.getByIcon(stripped) ?: return@visitSiblings
                text.font = MobIconsConfig.style.font
                icon.color?.let { color -> text.color = color }
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

    var color: Int? = null
    val short: String = short ?: name

    constructor(icon: Char, short: String? = null) : this(icon.toString(), short)

    companion object {
        private val cache: MutableMap<String, KnownMobIcon?> = ConcurrentHashMap()

        fun getByIcon(icon: String) = cache.getOrPut(icon) {
            KnownMobIcon.entries.find { it.icon == icon }
        }
    }
}
