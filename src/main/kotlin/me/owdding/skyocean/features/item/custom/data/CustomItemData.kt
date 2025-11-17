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
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.equipment.trim.TrimMaterial
import net.minecraft.world.item.equipment.trim.TrimPattern
import net.minecraft.world.item.equipment.trim.ArmorTrim as VanillaArmorTrim

@GenerateCodec
data class CustomItemData(
    val key: ItemKey,
    @NamedCodec("custom_item_component_map") val data: MutableMap<CustomItemComponent<*>, Any?> = mutableMapOf(),
) {
    operator fun <T> get(component: CustomItemComponent<T>): T? = data[component].unsafeCast()

    operator fun <T> set(component: CustomItemComponent<T>, value: T?) {
        if (value == null) {
            data.remove(component)
            return
        }

        data[component] = value as? Any
    }
}

data class CustomItemComponent<T>(
    val id: ResourceLocation,
    val codec: Codec<T>,
)

@GenerateCodec
data class ArmorTrim(
    val trimMaterial: ResourceLocation,
    val trimPattern: ResourceLocation,
) {
    val trim = getOrCreate(
        Registries.TRIM_MATERIAL.get(trimMaterial).value(),
        Registries.TRIM_PATTERN.get(trimPattern).value(),
    )

    constructor(trimMaterial: TrimMaterial, trimPattern: TrimPattern) : this(
        Registries.TRIM_MATERIAL.get(trimMaterial).unwrapKey().get().location(),
        Registries.TRIM_PATTERN.get(trimPattern).unwrapKey().get().location(),
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
    val registry = mutableMapOf<ResourceLocation, CustomItemComponent<*>>()
    val COMPONENT_CODEC: Codec<CustomItemComponent<*>> = ResourceLocation.CODEC.xmap({ registry[it] }, { it.id })

    @IncludedCodec(named = "custom_item_component_map")
    val COMPONENT_MAP_CODEC: Codec<MutableMap<CustomItemComponent<*>, Any?>> = LenientDispatchedMapCodec(
        COMPONENT_CODEC,
        CustomItemComponent<*>::codec,
    ).xmap({ HashMap(it) }, { it })

    val MODEL = register("model", SkyOceanCodecs.ItemModelCodec)
    val NAME: CustomItemComponent<Component> = register("name", CodecHelpers.CUSTOM_COMPONENT_CODEC)
    val ARMOR_TRIM: CustomItemComponent<ArmorTrim> = register("armor_trim", SkyOceanCodecs.ArmorTrimCodec)
    val COLOR = register("color", SkyOceanCodecs.ItemColorCodec)
    val SKIN = register("skin", SkyOceanCodecs.ItemSkinCodec)

    //val BLOCKING_ANIMATION: CustomItemComponent<Boolean> = register("block", Codec.BOOL)
    val ENCHANTMENT_GLINT_OVERRIDE: CustomItemComponent<Boolean> = register("glint", Codec.BOOL)
    //val ENCHANTMENT_GLINT_COLOR: CustomItemComponent<Int> = register("glint_color", Codec.INT)

    fun <T> register(id: String, codec: MapCodec<T>): CustomItemComponent<T> = register(id, codec.codec())
    fun <T> register(id: String, codec: Codec<T>): CustomItemComponent<T> {
        val id = SkyOcean.id(id)
        return CustomItemComponent(id, codec).apply { registry[id] = this }
    }
}
