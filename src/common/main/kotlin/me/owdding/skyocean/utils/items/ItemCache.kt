package me.owdding.skyocean.utils.items

import me.owdding.ktmodules.Module
import me.owdding.skyocean.utils.Utils.filterNotAir
import me.owdding.skyocean.utils.Utils.listEntries
import me.owdding.skyocean.utils.tags.ItemTagKey
import net.minecraft.tags.ItemTags

@Module
object ItemCache {

    val trimPatterns = ItemTagKey.TRIM_PATTERS.key.listEntries().filterNotAir()
    val trimMaterials = ItemTags.TRIM_MATERIALS.listEntries().filterNotAir()

}
