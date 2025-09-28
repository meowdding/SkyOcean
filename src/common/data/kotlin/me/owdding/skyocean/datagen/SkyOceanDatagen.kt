package me.owdding.skyocean.datagen

import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGenerator
import me.owdding.skyocean.datagen.dispatcher.SkyOceanDataGeneratorEntrypoint
import me.owdding.skyocean.datagen.font.FontIconsProvider
import me.owdding.skyocean.datagen.font.MobTypesFontProvider
import me.owdding.skyocean.datagen.models.FakeBlocksProvider
import me.owdding.skyocean.events.FakeBlockModelEventRegistrar
import me.owdding.skyocean.features.textures.KnownMobIcon
import me.owdding.skyocean.features.textures.MobIcons
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

object SkyOceanDatagen : SkyOceanDataGeneratorEntrypoint() {
    override val name: String = "included"

    override fun run(output: SkyOceanDataGenerator) {
        val createPack = output.createPack()
        createPack.register(::FakeBlocksProvider)
        createPack.register { MobTypesFontProvider(it, KnownMobIcon::name, MobIcons.MOB_ICONS) }
        createPack.register { MobTypesFontProvider(it, KnownMobIcon::short, MobIcons.MOB_ICONS_SHORT) }
        createPack.register { FontIconsProvider(it) }
    }

    operator fun FakeBlockModelEventRegistrar.invoke(block: Block, definition: ResourceLocation) {
        this(block, block, definition, null) { _, _ -> false }
    }

    operator fun FakeBlockModelEventRegistrar.invoke(block: Block, texture: Block, definition: ResourceLocation) {
        this(block, texture, definition, null) { _, _ -> false }
    }
}
