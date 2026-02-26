package me.owdding.skyocean.utils.codecs

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.lib.helper.TextShaderHolder
import me.owdding.lib.rendering.text.TextShaders
import me.owdding.skyocean.generated.CodecUtils
import me.owdding.skyocean.generated.SkyOceanCodecs
import me.owdding.skyocean.utils.PackMetadata
import net.minecraft.core.BlockPos
import net.minecraft.core.ClientAsset
import net.minecraft.network.chat.*
import net.minecraft.network.chat.contents.*
import net.minecraft.resources.Identifier
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.forNullGetter
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Function
import kotlin.jvm.optionals.getOrNull

internal fun createContentCodec(): MapCodec<ComponentContents> {
    val idMapper = ExtraCodecs.LateBoundIdMapper<String, MapCodec<out ComponentContents>>()
    idMapper.put("text", PlainTextContents.MAP_CODEC)
    idMapper.put("translatable", TranslatableContents.MAP_CODEC)
    idMapper.put("keybind", KeybindContents.MAP_CODEC)
    idMapper.put("score", ScoreContents.MAP_CODEC)
    idMapper.put("selector", SelectorContents.MAP_CODEC)
    idMapper.put("nbt", NbtContents.MAP_CODEC)
    idMapper.put("object", ObjectContents.MAP_CODEC)

    return ComponentSerialization.createLegacyComponentMatcher(idMapper, ComponentContents::codec, "type")
}

val PACK_FORMAT: Codec<PackMetadata> = SkyOceanCodecs.PackMetadataCodec.codec()

object CodecHelpers {

    @IncludedCodec
    val ITEM_STACK_CODEC: Codec<ItemStack> = ItemStack.OPTIONAL_CODEC

    @IncludedCodec
    val BLOCK_POS_CODEC: Codec<BlockPos> = BlockPos.CODEC

    @IncludedCodec
    val RESOURCE_LOCATION: Codec<Identifier> = Identifier.CODEC

    @IncludedCodec
    val COMPONENT_CODEC: Codec<Component> = ComponentSerialization.CODEC

    @IncludedCodec(keyable = true, named = "str_low")
    val STRING_LOWER: Codec<String> = Codec.STRING.xmap({ it.lowercase() }, { it })

    @IncludedCodec
    val SKYBLOCK_ID_UNKNOWN: Codec<SkyBlockId> = SkyBlockId.UNKNOWN_CODEC

    @IncludedCodec
    val CLIENT_ASSET_CODEC: Codec<ClientAsset> = Identifier.CODEC.xmap(
        { ClientAsset.ResourceTexture(it) },
        { it.id() },
    )

    val BLOCK_POS_STRING_CODEC: Codec<BlockPos> = Codec.STRING.xmap(
        { it.split(",").map { it.toInt() }.let { BlockPos(it[0], it[1], it[2]) } },
        { "${it.x},${it.y},${it.z}" },
    )

    val STYLE_WITH_SHADER_CODEC: MapCodec<Style> = RecordCodecBuilder.mapCodec {
        it.group(
            Style.Serializer.MAP_CODEC.forGetter(Function.identity()),
            TextShaders.CODEC.optionalFieldOf("text_shader").forNullGetter { style -> (style as? TextShaderHolder)?.`meowddinglib$getTextShader`() },
        ).apply(it) { style, shader ->
            (style as? TextShaderHolder)?.`meowddinglib$withTextShader`(shader.getOrNull())
        }
    }

    val CUSTOM_COMPONENT_CODEC: Codec<Component> = Codec.recursive("SkyOceanComponentCodec") { self ->
        val componentMatcher = createContentCodec()

        val codec: Codec<Component> = RecordCodecBuilder.create {
            it.group(
                componentMatcher.forGetter { it.contents },
                ExtraCodecs.nonEmptyList(self.listOf()).optionalFieldOf("extra", mutableListOf<Component>()).forGetter { it.siblings },
                STYLE_WITH_SHADER_CODEC.forGetter { it.style },
            ).apply(
                it,
            ) { contents: ComponentContents, siblings: List<Component>, style: Style ->
                MutableComponent(
                    contents,
                    siblings,
                    style,
                )
            }
        }

        return@recursive Codec.either(
            Codec.either(
                Codec.STRING,
                ExtraCodecs.nonEmptyList(self.listOf()),
            ),
            codec,
        ).xmap(
            {
                it.map(
                    { either ->
                        either.map(Component::literal, Text::join)
                    },
                    Function.identity(),
                )
            },
            {
                val collapsed = it.tryCollapseToString()
                if (collapsed != null) Either.left(Either.left(collapsed)) else Either.right(it)
            },
        )
    }

    fun <T, B> pair(t: Codec<T>, b: Codec<B>): Codec<Pair<T, B>> = RecordCodecBuilder.create {
        it.group(
            t.fieldOf("first").forGetter { it.first },
            b.fieldOf("second").forGetter { it.second },
        ).apply(it, { a, b -> a to b })
    }

    internal inline fun <reified K, reified V> map(): Codec<Map<K, V>> =
        Codec.unboundedMap(SkyOceanCodecs.getCodec<K>(), SkyOceanCodecs.getCodec<V>())

    internal inline fun <reified K, reified V> mutableMap(): Codec<MutableMap<K, V>> =
        CodecUtils.map(SkyOceanCodecs.getCodec<K>(), SkyOceanCodecs.getCodec<V>())

    internal inline fun <reified T> mutableList() = CodecUtils.mutableList(SkyOceanCodecs.getCodec<T>())
    internal inline fun <reified T> list(): Codec<List<T>> = SkyOceanCodecs.getCodec<T>().listOf()


    fun <A> unit(defaultValue: A): Codec<A> = unit { defaultValue }

    fun <A> unit(defaultValue: () -> A): Codec<A> = MapCodec.unit<A>(defaultValue).codec()

    fun <T> copyOnWriteList(original: Codec<T>): Codec<CopyOnWriteArrayList<T>> = original.listOf().xmap(
        { CopyOnWriteArrayList(it) },
        { it },
    )
}
