package me.owdding.skyocean.datagen.resourcepacks

import me.owdding.skyocean.datagen.SkyOceanDatagen.invoke
import me.owdding.skyocean.datagen.dispatcher.DatagenTarget
import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGenerator
import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGeneratorEntrypoint
import me.owdding.skyocean.datagen.models.FakeBlocksProvider
import me.owdding.skyocean.datagen.providers.CopyProvider
import me.owdding.skyocean.datagen.providers.PackMcMetaProvider
import me.owdding.skyocean.features.textures.CrystalHollowBlocks
import me.owdding.skyocean.utils.Utils.not
import net.minecraft.world.level.block.Blocks

object SkyOceanDeepHollows : SkyOceanDataGeneratorEntrypoint(DatagenTarget.RESOURCE_PACKS) {

    override val name: String = "Deep Hollows"

    override fun run(output: SkyOceanDataGenerator) {
        val deepHollows = output.createResourcePack("deep_hollows")
        deepHollows.register {
            FakeBlocksProvider(it, false) { registrar ->
                registrar(Blocks.DEEPSLATE, CrystalHollowBlocks.HARDSTONE)
                registrar(Blocks.DEEPSLATE_COAL_ORE, CrystalHollowBlocks.COAL_ORE)
                registrar(Blocks.DEEPSLATE_IRON_ORE, CrystalHollowBlocks.IRON_ORE)
                registrar(Blocks.DEEPSLATE_EMERALD_ORE, CrystalHollowBlocks.EMERALD_ORE)
                registrar(Blocks.DEEPSLATE_GOLD_ORE, CrystalHollowBlocks.GOLD_ORE)
                registrar(Blocks.DEEPSLATE_DIAMOND_ORE, CrystalHollowBlocks.DIAMOND_ORE)
                registrar(Blocks.DEEPSLATE_REDSTONE_ORE, CrystalHollowBlocks.REDSTONE_ORE)
                registrar(Blocks.DEEPSLATE_LAPIS_ORE, CrystalHollowBlocks.LAPIS_ORE)
                registrar(Blocks.COBBLED_DEEPSLATE, CrystalHollowBlocks.COBBLESTONE)
                registrar(Blocks.COBBLED_DEEPSLATE_SLAB, CrystalHollowBlocks.COBBLESTONE_SLAB)
                registrar(Blocks.COBBLED_DEEPSLATE_STAIRS, CrystalHollowBlocks.COBBLESTONE_STAIRS)
                registrar(Blocks.COBBLED_DEEPSLATE_WALL, CrystalHollowBlocks.COBBLESTONE_WALL)
            }
        }
        deepHollows.register {
            PackMcMetaProvider(it) {
                description = !"Converts all stone textures to deepslate in the crystal hollows"
            }
        }
        deepHollows.register {
            CopyProvider(it, "pack.png", "pack.png", "data/skyocean/textures/deep_hollows_small.png")
        }
    }
}
