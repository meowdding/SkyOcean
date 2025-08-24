package me.owdding.skyocean.datagen

import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.datagen.font.MobTypesFontProvider
import me.owdding.skyocean.datagen.models.FakeBlocksProvider
import me.owdding.skyocean.datagen.providers.PackMcMetaProvider
import me.owdding.skyocean.datagen.providers.SkyOceanDataGeneratorEntrypoint
import me.owdding.skyocean.events.FakeBlockModelEventRegistrar
import me.owdding.skyocean.features.textures.CrystalHollowBlocks
import me.owdding.skyocean.features.textures.KnownMobIcon
import me.owdding.skyocean.features.textures.MobIcons
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

object SkyOceanDatagen : SkyOceanDataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val createPack = fabricDataGenerator.createPack()
        createPack.register(::FakeBlocksProvider)
        createPack.register { MobTypesFontProvider(it, KnownMobIcon::name, MobIcons.MOB_ICONS) }
        createPack.register { MobTypesFontProvider(it, KnownMobIcon::short, MobIcons.MOB_ICONS_SHORT) }

        val deepHollows = fabricDataGenerator.createBuiltinResourcePack(SkyOcean.id("deep_hollows"))
        deepHollows.register {
            FakeBlocksProvider(it, false) { registrar ->
                registrar(Blocks.DEEPSLATE, CrystalHollowBlocks.HARDSTONE)
                registrar(Blocks.DEEPSLATE_COAL_ORE, CrystalHollowBlocks.COAL_ORE)
                registrar(Blocks.DEEPSLATE_IRON_ORE, CrystalHollowBlocks.IRON_ORE)
                registrar(Blocks.DEEPSLATE_EMERALD_ORE, CrystalHollowBlocks.EMERALD_ORE)
                registrar(Blocks.DEEPSLATE_GOLD_ORE, CrystalHollowBlocks.GOLD_ORE)
                registrar(Blocks.DEEPSLATE_DIAMOND_ORE, CrystalHollowBlocks.DIAMOND_ORE)
                registrar(Blocks.DEEPSLATE_REDSTONE_ORE, CrystalHollowBlocks.REDSTONE_ORE)
            }
        }
        deepHollows.register {
            PackMcMetaProvider(it) {
                description = "Converts all stone textures to deepslate in the crystal hollows"
            }
        }

    }

    operator fun FakeBlockModelEventRegistrar.invoke(block: Block, definition: ResourceLocation) {
        this(block, definition, null) { _, _ -> false }
    }
}
