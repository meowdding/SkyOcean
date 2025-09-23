package me.owdding.skyocean.features.misc.waypoints

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktcodecs.Unnamed
import me.owdding.skyocean.generated.SkyOceanCodecs
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3

@GenerateCodec
data class OrderedWaypointData(
    @NamedCodec("ordered_waypoint_color") @Unnamed val color: Int,
    @NamedCodec("named_vec3") @Unnamed val positon: Vec3,
    @NamedCodec("ordered_waypoint_name") @Unnamed val name: String,
) {
    companion object OrderedWaypointDataCodecs {

        @IncludedCodec(named = "ordered_waypoint_color")
        val COLOR_CODEC: MapCodec<Int> = SkyOceanCodecs.getMapCodec<OrderedWaypointColorData>().xmap(
            { (r, g, b) -> ARGB.colorFromFloat(1f, r, g, b) },
            { OrderedWaypointColorData(ARGB.redFloat(it), ARGB.greenFloat(it), ARGB.blueFloat(it)) },
        )

        @IncludedCodec(named = "ordered_waypoint_name")
        val NAME_CODEC: MapCodec<String> = Codec.either(
            Codec.STRING,
            Codec.INT,
        ).xmap({ Either.unwrap(it.mapRight { num -> num.toString() }) }, { Either.left(it) }).fieldOf("name").fieldOf("options")

    }
}

@GenerateCodec
data class OrderedWaypointColorData(
    val r: Float = 1f,
    val g: Float = 1f,
    val b: Float = 1f,
)
