package codes.cookies.skyocean.helpers.fakeblocks

import codes.cookies.skyocean.SkyOcean
import com.mojang.serialization.Lifecycle
import net.minecraft.core.DefaultedMappedRegistry
import net.minecraft.resources.ResourceKey

object FakeBlocks {

    val REGISTRY = DefaultedMappedRegistry<FakeBlock>("original", ResourceKey.createRegistryKey(SkyOcean.id("fake_blocks")), Lifecycle.stable(), false)


}
