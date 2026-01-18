package me.owdding.skyocean.features.keybinds.actions

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.util.ExtraCodecs

interface KeybindAction {

    val codec: MapCodec<out KeybindAction>
    fun perform()

    operator fun invoke() = perform()

}

object KeybindActions {
    val ID_MAPPER = ExtraCodecs.LateBoundIdMapper<String, MapCodec<out KeybindAction>>()

    @IncludedCodec
    val CODEC: Codec<KeybindAction> = ID_MAPPER.codec(Codec.STRING).dispatch(KeybindAction::codec) { it }

    init {
        ID_MAPPER.put("command", SkyOceanCodecs.CommandKeybindActionCodec)
        ID_MAPPER.put("key_mapping", SkyOceanCodecs.KeyMappingKeybindActionCodec)
    }
}
