package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.createTranslationKey
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.hash
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.utils.Utils.lookup
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.VillagerRenderState
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.npc.VillagerData
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerType
import kotlin.jvm.optionals.getOrNull

@RegisterAnimalModifier
object VillagerModifier : AnimalModifier<Villager, VillagerRenderState> {
    override val type: EntityType<Villager> = EntityType.VILLAGER
    val types = Registries.VILLAGER_TYPE.lookup().listElements().toList()
    val professions = Registries.VILLAGER_PROFESSION.lookup().listElements().toList()

    var villagerType = PlayerAnimalConfig.createEntry("villager_type") { id, type ->
        enum(id, Type.RANDOM) {
            this.translation = createTranslationKey("villager", "${type}_type")
            condition = isAnySelected(EntityType.VILLAGER, EntityType.ZOMBIE_VILLAGER)
        }
    }

    var villagerProfession = PlayerAnimalConfig.createEntry("villager_profession") { id, type ->
        enum(id, Profession.RANDOM) {
            this.translation = createTranslationKey("villager", "${type}_profession")
            condition = isAnySelected(EntityType.VILLAGER, EntityType.ZOMBIE_VILLAGER)
        }
    }

    override fun apply(
        avatarState: AvatarRenderState,
        state: VillagerRenderState,
        partialTicks: Float,
    ) {
        val randomSource = RandomSource.create(avatarState.hash.toLong())
        state.villagerData = VillagerData(
            villagerType.select(avatarState).villagerType ?: getRandom(avatarState, types),
            villagerProfession.select(avatarState).villagerProfession ?: getRandom(avatarState, professions),
            randomSource.nextInt(VillagerData.MIN_VILLAGER_LEVEL, VillagerData.MAX_VILLAGER_LEVEL),
        )
    }

    enum class Profession(val resourceKey: ResourceKey<VillagerProfession>?) : Translatable {
        RANDOM(null),
        NONE(VillagerProfession.NONE),
        ARMORER(VillagerProfession.ARMORER),
        BUTCHER(VillagerProfession.BUTCHER),
        CARTOGRAPHER(VillagerProfession.CARTOGRAPHER),
        CLERIC(VillagerProfession.CLERIC),
        FARMER(VillagerProfession.FARMER),
        FISHERMAN(VillagerProfession.FISHERMAN),
        FLETCHER(VillagerProfession.FLETCHER),
        LEATHERWORKER(VillagerProfession.LEATHERWORKER),
        LIBRARIAN(VillagerProfession.LIBRARIAN),
        MASON(VillagerProfession.MASON),
        NITWIT(VillagerProfession.NITWIT),
        SHEPHERD(VillagerProfession.SHEPHERD),
        TOOLSMITH(VillagerProfession.TOOLSMITH),
        WEAPONSMITH(VillagerProfession.WEAPONSMITH),
        ;

        val villagerProfession by lazy { resourceKey?.let { Registries.VILLAGER_PROFESSION.lookup().get(it).getOrNull() } }
        override fun getTranslationKey(): String =
            if (this == RANDOM) createTranslationKey("random") else "entity.minecraft.villager.${name.lowercase()}"
    }

    enum class Type(val resourceKey: ResourceKey<VillagerType>?) : Translatable {
        RANDOM(null),
        DESERT(VillagerType.DESERT),
        JUNGLE(VillagerType.JUNGLE),
        PLAINS(VillagerType.PLAINS),
        SAVANNA(VillagerType.SAVANNA),
        SNOW(VillagerType.SNOW),
        SWAMP(VillagerType.SWAMP),
        TAIGA(VillagerType.TAIGA),
        ;

        val villagerType by lazy { resourceKey?.let { Registries.VILLAGER_TYPE.lookup().get(it).getOrNull() } }
        override fun getTranslationKey(): String = createTranslationKey("villager", "type", name)
    }
}
