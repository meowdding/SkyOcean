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

    val FONT_ID = id("mob_types")

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
                (it as? MutableComponent)?.font = FONT_ID
            }
        }
    }

}

enum class KnownMobIcon(val icon: String) {
    UNDEAD('༕'),
    SKELETAL("🦴"),
    ENDER('⊙'),
    ATHROPOD('Ж'),
    HUMANOID('✰'),
    INFERNAL('♨'),
    CUBIC('⚂'),
    FROZEN('☃'),
    SOOKY('☽'),
    MYTHOLOGICAL('✿'),
    WITHER('☠'),
    SUBTERRANEAN('⛏'),
    AQUATIC('⚓'),
    PEST('ൠ'),
    ANIMAL('☮'),
    MAGMATIC('♆'),
    ELUSIVE('♣'),
    CONSTRUCT('⚙'),
    ARCANE('♃'),
    SHIELDED('⛨'),
    AIRBORNE('✈'),
    GLACIAL('❆'),
    WOODLAND('⸙')
    ;

    constructor(icon: Char) : this(icon.toString())

    companion object {
        private val cache: MutableMap<String, KnownMobIcon?> = ConcurrentHashMap()

        fun getByIcon(icon: String) = cache.getOrPut(icon) {
            KnownMobIcon.entries.find { it.icon == icon }
        }
    }
}
