package me.owdding.skyocean.features.misc.`fun`.animal.modifiers

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig
import me.owdding.skyocean.config.features.misc.`fun`.PlayerAnimalConfig.isSelected
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.AnimalModifier.Companion.hash
import me.owdding.skyocean.features.misc.`fun`.animal.RegisterAnimalModifier
import me.owdding.skyocean.features.misc.`fun`.animal.modifiers.TropicalFishModifier.Pattern
import me.owdding.skyocean.utils.Utils.list
import me.owdding.skyocean.utils.Utils.lookup
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.VillagerRenderState
import net.minecraft.client.renderer.entity.state.ZombieVillagerRenderState
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.ZombieVillager
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.npc.VillagerData
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerType
import kotlin.jvm.optionals.getOrNull

@RegisterAnimalModifier
object ZombieVillagerModifier : AnimalModifier<ZombieVillager, ZombieVillagerRenderState> {
    override val type: EntityType<ZombieVillager> = EntityType.ZOMBIE_VILLAGER

    override fun apply(
        avatarState: AvatarRenderState,
        state: ZombieVillagerRenderState,
        partialTicks: Float,
    ) {
        val randomSource = RandomSource.create(avatarState.hash.toLong())
        state.villagerData = VillagerData(
            VillagerModifier.villagerType.select(avatarState).villagerType ?: getRandom(avatarState, VillagerModifier.types),
            VillagerModifier.villagerProfession.select(avatarState).villagerProfession ?: getRandom(avatarState, VillagerModifier.professions),
            randomSource.nextInt(VillagerData.MIN_VILLAGER_LEVEL, VillagerData.MAX_VILLAGER_LEVEL),
        )
    }
}
