package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.AxolotlRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.axolotl.Axolotl

@RegisterAnimalModifier
object AxolotlModifier : AnimalModifier<Axolotl, AxolotlRenderState> {
    override val type: EntityType<Axolotl> = EntityType.AXOLOTL
    private val variants = Axolotl.Variant.entries

    var axolotlVariant = PlayerAnimalConfig.createEntry("axolotl_variant") { id, type ->
        enum(id, Variant.RANDOM) {
            this.translation = createTranslationKey("axolotl", "${type}_variant")
            condition = isSelected(EntityType.AXOLOTL)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: AxolotlRenderState,
        partialTicks: Float,
    ) {
        state.variant = axolotlVariant.select(avatarState).select(avatarState)
    }

    enum class Variant(val variant: Axolotl.Variant?) : Translatable {
        RANDOM(null),

        LUCY(Axolotl.Variant.LUCY),
        WILD(Axolotl.Variant.WILD),
        GOLD(Axolotl.Variant.GOLD),
        CYAN(Axolotl.Variant.CYAN),
        BLUE(Axolotl.Variant.BLUE),
        ;

        fun select(state: AvatarRenderState) = variant ?: getRandom(state, variants)
        override fun getTranslationKey(): String = createTranslationKey("axolotl", "variant", name)
    }
}
