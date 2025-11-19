package me.owdding.skyocean.datagen.models

import me.owdding.skyocean.SkyOcean
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.MultiVariant
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import java.util.*

val savedModels = mutableSetOf<ResourceLocation>()

abstract class BlockModelFactory {

    lateinit var generator: BlockModelGenerators
    abstract fun isFor(block: Block): Boolean
    abstract fun create(
        block: Block,
        texture: Block,
        fakeBlock: ResourceLocation,
        parent: ResourceLocation?,
        generator: BlockModelGenerators,
        modelGenContext: ModelGenContext,
    )

    fun ModelTemplate.plainVariant(
        location: ResourceLocation,
        block: Block,
        textureMapping: TextureMapping,
    ): MultiVariant {
        val apply = this.getDefaultModelLocation(block).let {
            ResourceLocation.fromNamespaceAndPath(
                location.namespace,
                it.path.replace(BuiltInRegistries.BLOCK.getKey(block).path, location.path),
            )
        }
        return BlockModelGenerators.plainVariant(create(apply, textureMapping, ::modelOutput))
    }

    fun getBlockModelLocation(location: ResourceLocation, suffix: String = ""): ResourceLocation = location.withPrefix("block/").withSuffix(suffix)

    fun getModelLocation(block: Block): ResourceLocation = BuiltInRegistries.BLOCK.getKey(block).withPrefix("block/")

    fun createCopy(block: Block, fakeBlock: ResourceLocation, parent: ResourceLocation?): ResourceLocation = ModelTemplate(
        parent.asOptional().map { getBlockModelLocation(it) }.orElse { getModelLocation(block) },
        Optional.empty(),
    ).create(
        getBlockModelLocation(fakeBlock),
        TextureMapping(),
        ::modelOutput,
    )

    fun <T : Any> T?.asOptional(): Optional<T> = Optional.ofNullable(this)
    fun <T : Any> Optional<T>.orElse(supplier: () -> T): Optional<T> = this.or { supplier().asOptional() }

    fun modelOutput(location: ResourceLocation, model: ModelInstance) {
        if (savedModels.contains(location)) {
            SkyOcean.info("Model with id $location already registered, skipping!")
            return
        }
        savedModels.add(location)
        generator.modelOutput.accept(location, model)
    }
}
