package me.owdding.skyocean.features.item.custom.data

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyocean.SkyOcean
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.Utils.get
import me.owdding.skyocean.utils.Utils.unsafeCast
import me.owdding.skyocean.utils.codecs.CodecHelpers
import me.owdding.skyocean.utils.codecs.LenientDispatchedMapCodec
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.equipment.trim.TrimMaterial
import net.minecraft.world.item.equipment.trim.TrimPattern
import tech.thatgravyboat.skyblockapi.platform.identifier
import net.minecraft.world.item.equipment.trim.ArmorTrim as VanillaArmorTrim

@GenerateCodec
data class CustomItemData(
    val key: ItemKey,
    @NamedCodec("custom_item_component_map") val data: MutableMap<CustomItemComponent<*>, Any> = mutableMapOf(),
) {
    operator fun <T : Any> get(component: CustomItemComponent<T>): T? = data[component].unsafeCast()

    operator fun <T : Any> set(component: CustomItemComponent<T>, value: T?) {
        if (value == null) {
            data.remove(component)
            return
        }

        data[component] = value as Any
    }
}

data class CustomItemComponent<T : Any>(
    val id: Identifier,
    val codec: Codec<T>,
)

@GenerateCodec
data class ArmorTrim(
    val trimMaterial: Identifier,
    val trimPattern: Identifier,
) {
    val trim = getOrCreate(
        Registries.TRIM_MATERIAL.get(trimMaterial).value(),
        Registries.TRIM_PATTERN.get(trimPattern).value(),
    )

    constructor(trimMaterial: TrimMaterial, trimPattern: TrimPattern) : this(
        Registries.TRIM_MATERIAL.get(trimMaterial).unwrapKey().get().identifier,
        Registries.TRIM_PATTERN.get(trimPattern).unwrapKey().get().identifier,
    )

    companion object {
        val cache = mutableMapOf<Pair<TrimMaterial, TrimPattern>, VanillaArmorTrim>()

        fun getOrCreate(trimMaterial: TrimMaterial, trimPattern: TrimPattern) = cache.getOrPut(trimMaterial to trimPattern) {
            VanillaArmorTrim(
                Registries.TRIM_MATERIAL.get(trimMaterial),
                Registries.TRIM_PATTERN.get(trimPattern),
            )
        }
    }
}

object CustomItemDataComponents {
    val registry = mutableMapOf<Identifier, CustomItemComponent<*>>()
    val COMPONENT_CODEC: Codec<CustomItemComponent<*>> = Identifier.CODEC.xmap({ registry[it] }, { it.id })

    @IncludedCodec(named = "custom_item_component_map")
    val COMPONENT_MAP_CODEC: Codec<MutableMap<CustomItemComponent<*>, Any>> = LenientDispatchedMapCodec(
        COMPONENT_CODEC,
        CustomItemComponent<*>::codec,
    ).xmap({ HashMap(it) }, { it })

    @JvmStatic
    @get:JvmName("model")
    val MODEL = register("model", SkyOceanCodecs.ItemModelCodec)

    @JvmStatic
    @get:JvmName("name")
    val NAME: CustomItemComponent<Component> = register("name", CodecHelpers.CUSTOM_COMPONENT_CODEC)

    @JvmStatic
    @get:JvmName("armorTrim")
    val ARMOR_TRIM: CustomItemComponent<ArmorTrim> = register("armor_trim", SkyOceanCodecs.ArmorTrimCodec)

    @JvmStatic
    @get:JvmName("color")
    val COLOR = register("color", SkyOceanCodecs.ItemColorCodec)

    @JvmStatic
    @get:JvmName("skin")
    val SKIN = register("skin", SkyOceanCodecs.ItemSkinCodec)

    //val BLOCKING_ANIMATION: CustomItemComponent<Boolean> = register("block", Codec.BOOL)
    val ENCHANTMENT_GLINT_OVERRIDE: CustomItemComponent<Boolean> = register("glint", Codec.BOOL)
    //val ENCHANTMENT_GLINT_COLOR: CustomItemComponent<Int> = register("glint_color", Codec.INT)

    fun <T : Any> register(id: String, codec: MapCodec<T>): CustomItemComponent<T> = register(id, codec.codec())
    fun <T : Any> register(id: String, codec: Codec<T>): CustomItemComponent<T> {
        val id = SkyOcean.id(id)
        return CustomItemComponent(id, codec).apply { registry[id] = this }
    }
}
