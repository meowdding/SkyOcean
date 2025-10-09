package me.owdding.skyocean.features.textures

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.SkyOcean.id
import me.owdding.skyocean.config.features.misc.MobIconsConfig
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.visitSiblings
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.GsonHelper
import net.minecraft.util.profiling.ProfilerFiller
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.entity.EntityInfoLineEvent
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font
import java.util.concurrent.atomic.AtomicBoolean

@Module
object MobIcons {

    val MOB_ICONS = id("mob_icons")
    val MOB_ICONS_SHORT = id("mob_icons/short")
    private val DEFAULT_SETTINGS = MobIconSettings(shouldColor = true)

    internal var settings: Map<KnownMobIcon, MobIconSettings> = emptyMap()

    enum class DisplayType(val font: ResourceLocation) {
        NORMAL(MOB_ICONS),
        SHORT(MOB_ICONS_SHORT),
    }

    private val MOB_TYPES = Regex(KnownMobIcon.entries.joinToString("|") { it.icon })

    init {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(MobIconSettingsListener)
    }

    fun getSetting(icon: KnownMobIcon): MobIconSettings = settings[icon] ?: DEFAULT_SETTINGS

    @Subscription
    @OnlyOnSkyBlock
    fun attachmentModifyEvent(event: EntityInfoLineEvent) {
        if (!MobIconsConfig.enabled) return
        val stripped = event.literalComponent
        if (!stripped.contains(MOB_TYPES)) return
        val hasFoundEnd = AtomicBoolean()
        event.component.visitSiblings {
            if (hasFoundEnd.get()) return@visitSiblings
            val stripped = it.stripped
            val trimmed = stripped.trim()
            if (trimmed.matches(MOB_TYPES)) {
                val text = (it as? MutableComponent) ?: return@visitSiblings
                val icon = KnownMobIcon.getByIcon(trimmed) ?: return@visitSiblings
                text.font = MobIconsConfig.style.font

                val settings = getSetting(icon)

                if (settings.shouldColor) {
                    icon.color?.let { color -> text.color = color }
                } else {
                    text.color = TextColor.WHITE
                }
                if (stripped.endsWith(" ")) {
                    hasFoundEnd.set(true)
                }
            }
        }
    }
}

private object MobIconSettingsListener : SimplePreparableReloadListener<Map<KnownMobIcon, MobIconSettings>>(), IdentifiableResourceReloadListener {

    private val CODEC = CodecUtils.map(SkyOceanCodecs.getCodec<KnownMobIcon>(), SkyOceanCodecs.getCodec<MobIconSettings>())

    override fun prepare(manager: ResourceManager, profiler: ProfilerFiller): Map<KnownMobIcon, MobIconSettings> =
        manager.getResourceStack(id("mob_icon_settings.json"))
            .map { resource ->
                runCatching {
                    resource.openAsReader().use { GsonHelper.parse(it).toDataOrThrow(CODEC) }
                }.getOrElse {
                    SkyOcean.error("Failed to load mob icon settings.", it)
                    emptyMap()
                }
            }
            .fold(emptyMap()) { acc, map -> acc + map }

    override fun apply(data: Map<KnownMobIcon, MobIconSettings>, manager: ResourceManager, profiler: ProfilerFiller) {
        MobIcons.settings = data
    }

    override fun getFabricId(): ResourceLocation = id("mob_icon_settings")

}

@GenerateCodec
data class MobIconSettings(val shouldColor: Boolean)

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
    GLACIAL('❄', "GLAC"),
    WOODLAND('⸙', "WOOD"),
    ;

    var color: Int? = null
    val short: String = short ?: name

    constructor(icon: Char, short: String? = null) : this(icon.toString(), short)

    companion object {
        private val cache: Map<String, KnownMobIcon> = KnownMobIcon.entries.associateBy(KnownMobIcon::icon)

        fun getByIcon(icon: String) = cache[icon]
    }
}
