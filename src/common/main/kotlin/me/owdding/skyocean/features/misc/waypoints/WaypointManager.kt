package me.owdding.skyocean.features.misc.waypoints

import me.owdding.lib.waypoints.MeowddingWaypoint
import me.owdding.lib.waypoints.WaypointRenderType
import me.owdding.skyocean.utils.OceanColors
import me.owdding.skyocean.utils.Utils
import me.owdding.skyocean.utils.extensions.orZero
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import kotlin.math.abs

object WaypointManager {

    val waypointGroups: MutableMap<String, List<MeowddingWaypoint>> = mutableMapOf()

    fun addOrdered(name: String, entries: List<OrderedWaypointData>) {
        var currentIndex = 0
        val waypoints =
            entries.sortedBy { (_, _, name) -> if (name.toIntOrNull() != null) name.toInt() else Int.MAX_VALUE }.mapIndexed { index, (color, position, name) ->
                MeowddingWaypoint(position) {
                    withName(name)
                    withRenderCondition { _ ->
                        val distanceToPlayer = McPlayer.position.orZero().distanceToSqr(position).toFloat()

                        if (abs(currentIndex - index) <= 1 || (currentIndex == entries.size && index == 0)) {
                            if (distanceToPlayer < 12 && currentIndex != index) {
                                currentIndex = index
                            }
                            if (index > currentIndex) {
                                withRenderTypes(
                                    WaypointRenderType.TEXT,
                                    WaypointRenderType.DISTANCE,
                                    WaypointRenderType.BOX,
                                    WaypointRenderType.TRACER,
                                )
                            } else {
                                withNormalRenderTypes()
                            }
                            withColor(
                                ARGB.opaque(
                                    color.takeUnless { index != currentIndex }
                                        ?: if (index < currentIndex) {
                                            OceanColors.WARNING
                                        } else {
                                            OceanColors.DARK_CYAN_BLUE
                                        },
                                ),
                            )
                            return@withRenderCondition true
                        }

                        if (distanceToPlayer > 25) {
                            return@withRenderCondition false
                        }

                        if (distanceToPlayer <= 1) {
                            currentIndex = index
                        }

                        withRenderTypes(
                            WaypointRenderType.TEXT,
                            WaypointRenderType.DISTANCE,
                            WaypointRenderType.BOX,
                        )
                        withColor(Utils.color(1 - distanceToPlayer / 25, 0xFF0000))
                        true
                    }
                    withNormalRenderTypes()
                }
            }

        waypointGroups[name] = waypoints
    }

}
