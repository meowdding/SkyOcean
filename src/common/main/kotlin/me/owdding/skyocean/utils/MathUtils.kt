package me.owdding.skyocean.utils

object MathUtils {

    fun lerp(progress: Double, start: Int, end: Int) = start + (progress * (end - start)).toInt()

}
