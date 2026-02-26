package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import earth.terrarium.olympus.client.utils.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.modifiers.WolfModifier.State.ANGRY
import me.owdding.skyocean.features.misc.`fun`.animal.modifiers.WolfModifier.State.TAME
import me.owdding.skyocean.features.misc.`fun`.animal.modifiers.WolfModifier.State.WILD
import me.owdding.skyocean.utils.Utils.list
import me.owdding.skyocean.utils.Utils.lookup
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.WolfRenderState
import net.minecraft.core.ClientAsset
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.wolf.Wolf
import net.minecraft.world.entity.animal.wolf.WolfVariant
import net.minecraft.world.entity.animal.wolf.WolfVariants
import kotlin.jvm.optionals.getOrNull

@RegisterAnimalModifier
object WolfModifier : AnimalModifier<Wolf, WolfRenderState> {
    override val type: EntityType<Wolf> = EntityType.WOLF

    private val wolfVariants: List<WolfVariant> = Registries.WOLF_VARIANT.list().sortedBy { it.assetInfo.tame.toString() }
    private val states = listOf(TAME, WILD, ANGRY)

    var wolfVariant = PlayerAnimalConfig.createEntry("wolf_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("wolf", "${type}_variant")
            condition = isSelected(EntityType.WOLF)
        }
    }

    var wolfState = PlayerAnimalConfig.createEntry("wolf_state") { id, type ->
        enum(id, State.RANDOM) {
            this.translation = createTranslationKey("wolf", "${type}_state")
            condition = isSelected(EntityType.WOLF)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: WolfRenderState,
        partialTicks: Float,
    ) {
        state.collarColor = getCollarColor(avatarState)
        val variant = wolfVariant.select(avatarState).wolfVariant ?: getRandom(avatarState, wolfVariants)
        state.texture = wolfState.select(avatarState).select(avatarState, variant).texturePath()
        state.isSitting = avatarState.isCrouching
    }

    enum class State(val selector: ((WolfVariant) -> ClientAsset.ResourceTexture)) : Translatable {
        RANDOM({ it.assetInfo.tame }),

        TAME({ it.assetInfo.tame }),
        WILD({ it.assetInfo.wild }),
        ANGRY({ it.assetInfo.angry }),
        ;

        fun select(state: AvatarRenderState, wolfVariant: WolfVariant) = if (this == RANDOM) {
            getRandom(state, states)
        } else {
            this
        }.selector(wolfVariant)

        override fun getTranslationKey(): String = createTranslationKey("wolf", "state", name)
    }

    enum class Variant(val resourceKey: ResourceKey<WolfVariant>?) : Translatable {
        RANDOM(null),

        PALE(WolfVariants.PALE),
        SPOTTED(WolfVariants.SPOTTED),
        SNOWY(WolfVariants.SNOWY),
        BLACK(WolfVariants.BLACK),
        ASHEN(WolfVariants.ASHEN),
        RUSTY(WolfVariants.RUSTY),
        WOODS(WolfVariants.WOODS),
        CHESTNUT(WolfVariants.CHESTNUT),
        STRIPED(WolfVariants.STRIPED),
        ;

        val wolfVariant by lazy { resourceKey?.let { Registries.WOLF_VARIANT.lookup().get(it).getOrNull() }?.value() }
        override fun getTranslationKey(): String = createTranslationKey("wolf", "variant", name)
    }
}
