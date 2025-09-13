package me.owdding.skyocean.datagen.resourcepacks

import me.owdding.skyocean.datagen.SkyOceanDatagen.invoke
import me.owdding.skyocean.datagen.dispatcher.DatagenTarget
import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGenerator
import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGeneratorEntrypoint
import me.owdding.skyocean.datagen.models.FakeBlocksProvider
import me.owdding.skyocean.datagen.providers.CopyProvider
import me.owdding.skyocean.datagen.providers.PackMcMetaProvider
import me.owdding.skyocean.features.textures.GlaciteBlocks
import me.owdding.skyocean.utils.Utils.not
import net.minecraft.world.level.block.Blocks

object SkyOceanOvergrownTunnels : SkyOceanDataGeneratorEntrypoint(DatagenTarget.RESOURCE_PACKS) {

    override val name: String = "Overgrown Tunnels"

    override fun run(output: SkyOceanDataGenerator) {
        val overgrownTunnels = output.createResourcePack("overgrown_tunnels")
        overgrownTunnels.register {
            FakeBlocksProvider(it, false) { registrar ->
                registrar(Blocks.SNOW, Blocks.MOSS_BLOCK, GlaciteBlocks.GLACITE_SNOW)
                registrar(Blocks.SNOW_BLOCK, Blocks.MOSS_BLOCK, GlaciteBlocks.GLACITE_SNOW_BLOCK)
                registrar(Blocks.AMETHYST_BLOCK, GlaciteBlocks.GLACITE_BLOCK)
                registrar(Blocks.SMOOTH_BASALT, GlaciteBlocks.GLACITE_HARD_STONE)
                registrar(Blocks.SMOOTH_BASALT, GlaciteBlocks.GLACITE_HARD_STONE_WOOL)
            }
        }
        overgrownTunnels.register {
            PackMcMetaProvider(it) {
                description = !"Makes the glacite tunnels overgrown instead of frozen!"
            }
        }
        overgrownTunnels.register {
            CopyProvider(it, "pack.png", "pack.png", "data/skyocean/textures/overgrown_tunnels_small.png")
        }
    }

}
