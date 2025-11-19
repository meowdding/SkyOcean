package me.owdding.skyocean.utils.rendering

import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import earth.terrarium.olympus.client.pipelines.uniforms.RenderPipelineUniforms
import earth.terrarium.olympus.client.pipelines.uniforms.RenderPipelineUniformsStorage
import net.minecraft.client.renderer.DynamicUniformStorage
import org.joml.Vector2i
import java.nio.ByteBuffer
import java.util.function.Supplier

const val MONO_UNIFORM_NAME = "MonoInventoryUniform"

data class MonoInventoryUniform(
    val size: Int,
    val vertical: Int,
) : RenderPipelineUniforms {
    override fun name() = MONO_UNIFORM_NAME

    override fun write(byteBuffer: ByteBuffer) {
        Std140Builder.intoBuffer(byteBuffer)
            .putInt(size)
            .putInt(vertical)
            .get()
    }

    companion object {
        val STORAGE: Supplier<DynamicUniformStorage<MonoInventoryUniform>> =
            RenderPipelineUniformsStorage.register<MonoInventoryUniform>("SkyOcean Mono Inventory UBO", 5, Std140SizeCalculator().putInt().putInt())
    }
}

const val POLY_UNIFORM_NAME = "PolyInventoryUniform"

data class PolyInventoryUniform(
    val size: Vector2i,
) : RenderPipelineUniforms {
    override fun name() = POLY_UNIFORM_NAME

    override fun write(byteBuffer: ByteBuffer) {
        Std140Builder.intoBuffer(byteBuffer)
            .putIVec2(size)
            .get()
    }

    companion object {
        val STORAGE: Supplier<DynamicUniformStorage<PolyInventoryUniform>> =
            RenderPipelineUniformsStorage.register<PolyInventoryUniform>("SkyOcean Poly Inventory UBO", 5, Std140SizeCalculator().putIVec2())
    }
}
