package codes.cookies.skyocean.helpers.fakeblocks

import com.google.common.base.Splitter
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.renderer.block.model.BlockModelPart
import net.minecraft.client.renderer.block.model.SimpleModelWrapper
import net.minecraft.client.resources.model.BlockModelRotation
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.Property
import kotlin.jvm.optionals.getOrNull

class FakeBlockStateDefinition(
    val variants: Map<BlockStateKey, ResourceLocation>
) {

    fun init(block: Block, baker: ModelBaker): Map<BlockState, BlockModelPart> {
        val parts = mutableMapOf<ResourceLocation, BlockModelPart>()
        val result = mutableMapOf<BlockState, BlockModelPart>()
        
        for ((key, value) in variants) {
            val states = key.getStates(block)
            for (state in states) {
                result[state] = parts.computeIfAbsent(value) { id ->
                    SimpleModelWrapper.bake(baker, id, BlockModelRotation.X0_Y0)
                }
            }
        }

        return result
    }

    companion object {

        val CODEC: Codec<FakeBlockStateDefinition> = RecordCodecBuilder.create { it.group(
            Codec.unboundedMap(BlockStateKey.CODEC, ResourceLocation.CODEC).fieldOf("variants").forGetter(FakeBlockStateDefinition::variants)
        ).apply(it, ::FakeBlockStateDefinition) }
    }
}

data class BlockStateKey(private val value: String) {

    private var states: List<BlockState>? = null

    fun getStates(block: Block): List<BlockState> {
        if (states == null) {
            states = block.stateDefinition.possibleStates.filter(parseStatePredicate(block.stateDefinition, value)).toList()
            if (states.isNullOrEmpty()) {
                throw IllegalArgumentException("No states found for $value")
            }
        }
        return states ?: emptyList()
    }

    companion object {

        private val COMMA_SPLITTER = Splitter.on(",")
        private val EQUAL_SPLITTER = Splitter.on("=").limit(2)

        val CODEC: Codec<BlockStateKey> = Codec.STRING.xmap(::BlockStateKey, BlockStateKey::value)

        private fun parseStatePredicate(definition: StateDefinition<Block, BlockState>, value: String): (BlockState?) -> Boolean {
            if (value.isEmpty()) return { true }

            val properties = mutableMapOf<Property<*>, Comparable<*>>()

            for (property in COMMA_SPLITTER.split(value)) {
                val parts = EQUAL_SPLITTER.splitToList(property)
                if (parts.size == 2) error("Invalid state predicate: $value")

                var (key, value) = parts
                val blockProperty = definition.getProperty(key) ?: error("Unknown property: $key")
                val propertyValue = blockProperty.getValue(value).getOrNull() ?: error("Invalid value: $value")

                properties[blockProperty] = propertyValue
            }

            return { state ->
                val statement = state
                    ?.takeIf { state -> state.block == (definition.owner) }
                    ?.let { state -> properties.all { (property, value) -> value == state.getValue(property) } }
                statement == true
            }
        }
    }
}
