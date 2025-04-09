package codes.cookies.skyocean.datagen.models

import codes.cookies.skyocean.helpers.fakeblocks.FakeBlockEntry
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.MultiVariant
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

interface BlockModelFactory {

    fun isFor(block: Block): Boolean
    fun create(
        block: Block,
        fakeBlock: FakeBlockEntry,
        generator: BlockModelGenerators,
        modelGenContext: ModelGenContext,
    )

    fun ModelTemplate.plainVariant(
        location: ResourceLocation,
        block: Block,
        textureMapping: TextureMapping,
        generator: BlockModelGenerators,
    ): MultiVariant {
        val apply = this.getDefaultModelLocation(block).let {
            ResourceLocation.fromNamespaceAndPath(
                location.namespace,
                it.path.replace(BuiltInRegistries.BLOCK.getKey(block).path, location.path)
            )
        }
        return BlockModelGenerators.plainVariant(create(apply, textureMapping, generator.modelOutput))
    }

    fun getBlockModelLocation(location: ResourceLocation, suffix: String = ""): ResourceLocation {
        return location.withPrefix("block/").withSuffix(suffix)
    }
}
