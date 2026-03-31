package me.owdding.skyocean.datagen

import me.owdding.skyocean.utils.tags.EntityTagKey
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.util.concurrent.CompletableFuture

class EntityTagProvider(output: FabricPackOutput, future: CompletableFuture<HolderLookup.Provider>) : FabricTagsProvider.EntityTypeTagsProvider(output, future) {
    override fun addTags(wrapperLookup: HolderLookup.Provider) {
        val builder = valueLookupBuilder(EntityTagKey.LIVING_ENTITIES.key)

        EntityType::class.java.declaredFields.filter { Modifier.isStatic(it.modifiers) && EntityType::class.java.isAssignableFrom(it.type) }
            .associate { (it.genericType as? ParameterizedType)?.actualTypeArguments?.firstOrNull() as? Class<*> to it.get(null) as EntityType<*> }
            .filter { it.key != null && LivingEntity::class.java.isAssignableFrom(it.key!!) }.map { it.value }.forEach(builder::add)
    }
}
