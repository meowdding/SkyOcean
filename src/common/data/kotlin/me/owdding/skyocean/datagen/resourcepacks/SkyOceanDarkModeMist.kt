package me.owdding.skyocean.datagen.resourcepacks

import me.owdding.skyocean.datagen.SkyOceanDatagen.invoke
import me.owdding.skyocean.datagen.dispatcher.DatagenTarget
import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGenerator
import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGeneratorEntrypoint
import me.owdding.skyocean.datagen.models.FakeBlocksProvider
import me.owdding.skyocean.datagen.providers.CopyProvider
import me.owdding.skyocean.datagen.providers.PackMcMetaProvider
import me.owdding.skyocean.features.textures.MistBlocks
import me.owdding.skyocean.utils.Utils.not
import net.minecraft.world.level.block.Blocks

object SkyOceanDarkModeMist : SkyOceanDataGeneratorEntrypoint(DatagenTarget.RESOURCE_PACKS) {

    override val name: String = "Deep Hollows"

    override fun run(output: SkyOceanDataGenerator) {
        val deepHollows = output.createResourcePack("darkmode_mist")
        deepHollows.register {
            FakeBlocksProvider(it, false) { registrar ->
                registrar(Blocks.BLACK_STAINED_GLASS, MistBlocks.MIST_GLASS)
                registrar(Blocks.GRAY_STAINED_GLASS, MistBlocks.MIST_GLASS_SECONDARY)
                registrar(Blocks.BLACK_CONCRETE_POWDER, MistBlocks.MIST_CLAY)
                registrar(Blocks.BLACK_CARPET, MistBlocks.MIST_CARPET)
                registrar(Blocks.BLACK_STAINED_GLASS_PANE, MistBlocks.MIST_BLUE_GLASS_PANE)
                registrar(Blocks.POLISHED_BLACKSTONE, MistBlocks.MIST_ICE)
                registrar(Blocks.GRAY_STAINED_GLASS_PANE, MistBlocks.MIST_LIGHT_BLUE_GLASS_PANE)
                registrar(Blocks.BLACKSTONE, MistBlocks.MIST_SNOW_BLOCK)
                registrar(Blocks.BLACKSTONE, MistBlocks.MIST_SNOW)
            }
        }
        deepHollows.register {
            PackMcMetaProvider(it) {
                description = !"Makes the mist dark!"
            }
        }
        deepHollows.register {
            CopyProvider(it, "pack.png", "pack.png", "data/skyocean/textures/darkmode_mist_small.png")
        }
    }
}
