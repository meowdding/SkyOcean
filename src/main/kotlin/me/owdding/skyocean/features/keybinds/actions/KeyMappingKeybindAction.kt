package me.owdding.skyocean.features.keybinds.actions

import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.client.KeyMapping

@GenerateCodec
data class KeyMappingKeybindAction(
    @FieldName("key_name") val keyName: String
) : KeybindAction {
    override val codec: MapCodec<KeyMappingKeybindAction> = SkyOceanCodecs.KeyMappingKeybindActionCodec

    override fun perform() {
        KeyMapping.get(keyName)?.clickCount++
    }
}
