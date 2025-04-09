package codes.cookies.skyocean.features.textures

import codes.cookies.skyocean.SkyOcean
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour.Properties

open class BlockRetexture {
    val map = mutableMapOf<Block, Block>()

    fun register(name: String, properties: Properties = Properties.of()) = register(name, ::Block, properties)
    fun register(name: String, factory: (Properties) -> Block, properties: Properties = Properties.of()): Block {
        val resourceKey = ResourceKey.create(Registries.BLOCK, SkyOcean.id(name))
        properties.setId(resourceKey)
        return Registry.register(BuiltInRegistries.BLOCK, resourceKey, factory(properties))
    }

    open fun registerMultiple(defaultBlocks: Array<Block>, newBlock: Block): Block {
        defaultBlocks.forEach {
            map[it] = newBlock
        }
        return newBlock
    }

    open fun register(defaultBlock: Block, newBlock: Block): Block {
        map[defaultBlock] = newBlock
        return newBlock
    }
}
