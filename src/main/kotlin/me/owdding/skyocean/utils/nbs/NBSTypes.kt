package me.owdding.skyocean.utils.nbs

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.nio.charset.StandardCharsets

internal object NBSTypes {
    val BYTE: StreamCodec<ByteBuf, Byte> = ByteBufCodecs.BYTE

    val UNSIGNED_BYTE: StreamCodec<ByteBuf, Short> = StreamCodec.of(
        { buf, value -> buf.writeByte(value.toInt()) },
        { buf -> buf.readUnsignedByte() },
    )

    val BOOL: StreamCodec<ByteBuf, Boolean> = ByteBufCodecs.BOOL

    val SHORT: StreamCodec<ByteBuf, Short> = StreamCodec.of(
        { buf, value -> buf.writeShortLE(value.toInt()) },
        { buf -> buf.readShortLE() },
    )

    val INT: StreamCodec<ByteBuf, Int> = StreamCodec.of(
        { buf, value -> buf.writeIntLE(value) },
        { buf -> buf.readIntLE() },
    )

    val STRING: StreamCodec<ByteBuf, String> = object : StreamCodec<ByteBuf, String> {
        override fun decode(buffer: ByteBuf): String {
            val length = INT.decode(buffer)

            if (length > buffer.readableBytes()) {
                throw IndexOutOfBoundsException("String length $length exceeds readable bytes ${buffer.readableBytes()}")
            }

            val bytes = ByteArray(length)
            buffer.readBytes(bytes)
            return String(bytes, StandardCharsets.UTF_8)
        }

        override fun encode(buffer: ByteBuf, value: String) {
            val bytes = value.toByteArray(StandardCharsets.UTF_8)
            INT.encode(buffer, bytes.size)
            buffer.writeBytes(bytes)
        }
    }
}
