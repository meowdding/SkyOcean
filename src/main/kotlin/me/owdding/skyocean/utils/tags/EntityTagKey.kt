package me.owdding.skyocean.utils.tags

import me.owdding.skyocean.SkyOcean
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import tech.thatgravyboat.skyblockapi.impl.tagkey.BaseTagKey

enum class EntityTagKey(path: String) : BaseTagKey<EntityType<*>> {
    LIVING_ENTITIES("living_entities"),
    ;

    override val key: TagKey<EntityType<*>> = TagKey.create(Registries.ENTITY_TYPE, SkyOcean.id(path))
}
