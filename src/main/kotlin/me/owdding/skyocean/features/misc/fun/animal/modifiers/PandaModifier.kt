package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.PandaRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Panda

@RegisterAnimalModifier
object PandaModifier : AnimalModifier<Panda, PandaRenderState> {
    override val type: EntityType<Panda> = EntityType.PANDA
    val pandaGenes = Panda.Gene.entries

    var pandaGene = PlayerAnimalConfig.createEntry("panda_gene") { id, type ->
        enum(id, Gene.RANDOM) {
            this.translation = createTranslationKey("panda", "${type}_gene")
            condition = isSelected(EntityType.PANDA)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: PandaRenderState,
        partialTicks: Float,
    ) {
        state.variant = pandaGene.select(avatarState).gene ?: getRandom(avatarState, pandaGenes)
    }

    enum class Gene(val gene: Panda.Gene?) : Translatable {
        RANDOM(null),

        NORMAL(Panda.Gene.NORMAL),
        LAZY(Panda.Gene.LAZY),
        WORRIED(Panda.Gene.WORRIED),
        PLAYFUL(Panda.Gene.PLAYFUL),
        BROWN(Panda.Gene.BROWN),
        WEAK(Panda.Gene.WEAK),
        AGGRESSIVE(Panda.Gene.AGGRESSIVE),
        ;

        override fun getTranslationKey(): String = createTranslationKey("panda", "gene", name)
    }
}
