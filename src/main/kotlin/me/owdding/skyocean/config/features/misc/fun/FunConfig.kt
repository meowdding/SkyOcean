package me.owdding.skyocean.config.features.misc.`fun`

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.skyocean.config.utils.GenericDropdown.Companion.entityTypeDropdown
import me.owdding.skyocean.features.misc.`fun`.animal.PlayerAnimals
import me.owdding.skyocean.utils.tags.EntityTagKey
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType

object FunConfig : CategoryKt("fun") {
    override val name get() = Translated("skyocean.config.misc.fun")

    var playerAnimals by enum(PlayerAnimals.PlayerAnimalState.NONE) {
        this.translation = "skyocean.config.misc.fun.player_animals"
    }

    val entityType: EntityType<*> by entityTypeDropdown(
        EntityType.CAT,
        BuiltInRegistries.ENTITY_TYPE.toList().filter { it in EntityTagKey.LIVING_ENTITIES },
    ) {
        this.translation = "skyocean.config.misc.fun.player_animals.type"
    }

    val outsideSkyblock by boolean(false) {
        this.translation = "skyocean.config.misc.fun.player_animals.outside_skyblock"
    }

    init {
        obj("player_animals_config", PlayerAnimalConfig) {
            this.translation = "skyocean.config.misc.fun.player_animals.config"
        }
        PlayerAnimals.registerModifiers()
    }

}
