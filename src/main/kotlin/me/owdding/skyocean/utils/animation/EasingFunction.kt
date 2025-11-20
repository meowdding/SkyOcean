package me.owdding.skyocean.utils.animation

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


// Math from https://easings.net
@Suppress("unused")
object EasingFunctions {
    private fun easeInN(power: Int) = EasingFunction { it.pow(power) }
    private fun easeOutN(power: Int) = EasingFunction { 1 - (1 - it).pow(power) }
    private fun easeInOutN(power: Int): EasingFunction {
        val factor = 2.0.pow(power - 1)
        return EasingFunction { if (it < 0.5) factor * it.pow(power) else 1 - (-2 * it + 2).pow(power) / 2 }
    }

    val easeInSine = EasingFunction { 1 - cos((it * Math.PI) / 2) }
    val easeOutSine = EasingFunction { sin((it * Math.PI) / 2) }
    val easeInOutSine = EasingFunction { -(cos(Math.PI * it) - 1) / 2 }

    val easeInQuad = easeInN(2)
    val easeOutQuad = easeOutN(2)
    val easeInOutQuad = easeInOutN(2)

    val easeInCubic = easeInN(3)
    val easeOutCubic = easeOutN(3)
    val easeInOutCubic = easeInOutN(3)

    val easeInQuart = easeInN(4)
    val easeOutQuart = easeOutN(4)
    val easeInOutQuart = easeInOutN(4)

    val easeInQuint = easeInN(5)
    val easeOutQuint = easeOutN(5)
    val easeInOutQuint = easeInOutN(5)

    val easeInExpo = EasingFunction { if (it == 0.0) 0.0 else 2.0.pow(10 * it - 10) }
    val easeOutExpo = EasingFunction { if (it == 1.0) 1.0 else 1 - 2.0.pow(-10 * it) }
    val easeInOutExpo = EasingFunction {
        if (it == 1.0 || it == 0.0) {
            return@EasingFunction it
        }
        if (it < 0.5) {
            return@EasingFunction 2.0.pow(20 * it - 10) / 2
        }

        (2 - 2.0.pow(-20 * it + 10)) / 2
    }

    val easeInCirc = EasingFunction { 1 - sqrt(1 - it.pow(2)) }
    val easeOutCirc = EasingFunction { sqrt(1 - (it - 1).pow(2)) }
    val easeInOutCirc = EasingFunction { if (it < 0.5) (1 - sqrt(1 - (2 * it).pow(2))) / 2 else (sqrt(1 - (-2 * it + 2).pow(2)) + 1) / 2 }

    val easeInBack = EasingFunction { 2.70158 * it.pow(3) - 1.70158 * it.pow(2) }
    val easeOutBack = EasingFunction { 1 + 2.70158 * (it - 1).pow(3) + 1.70158 * (it - 1).pow(2) }
    val easeInOutBack = EasingFunction {
        if (it < 0.5) {
            ((2 * it).pow(2) * ((2.5949095 + 1) * 2 * it - 2.5949095)) / 2
        } else {
            ((2 * it - 2).pow(2) * ((2.5949095 + 1) * (it * 2 - 2) + 2.5949095) + 2) / 2
        }
    }

    val easeInElastic = run {
        val constant = (2 * Math.PI) / 3

        EasingFunction {
            if (it == 0.0 || it == 1.0) {
                it
            } else {
                (-2.0).pow(10 * it - 10) * sin((it * 10 - 10.75) * constant)
            }
        }
    }
    val easeOutElastic = run {
        val constant = (2 * Math.PI) / 3

        EasingFunction {
            if (it == 0.0 || it == 1.0) {
                it
            } else {
                2.0.pow(-10 * it) * sin((it * 10 - 0.75) * constant) + 1
            }
        }
    }
    val easeInOutElastic = run {
        val constant = (2 * Math.PI) / 4.5
        EasingFunction {
            if (it == 0.0 || it == 1.0) {
                it
            } else if (it < 0.5) {
                -(2.0.pow(20 * it - 10) * sin((20 * it - 11.125) * constant)) / 2
            } else {
                (2.0.pow(-20 * it + 10) * sin((20 * it - 11.125) * constant)) / 2 + 1
            }
        }
    }

    val easeOutBounce = run {
        val constant1 = 7.5625
        val constant2 = 2.75

        EasingFunction {
            if (it < 1 / constant2) {
                constant1 * it.pow(2)
            } else if (it < 2 / constant2) {
                val it = (it - 1.5)
                constant1 * (it / constant2) * it + 0.75
            } else if (it < 2.5 / constant2) {
                val it = (it - 2.25)
                constant1 * (it / constant2) * it + 0.9375
            } else {
                val it = it - 2.625
                constant1 * (it / constant2) * it + 0.984375
            }
        }
    }
    val easeInBounce = EasingFunction { 1 - easeOutBounce(1 - it) }
    val easeInOutBounce = EasingFunction {
        if (it < 0.5) {
            (1 - easeOutBounce(1 - 2 * it)) / 2
        } else {
            (1 + easeOutBounce(2 * it - 1)) / 2
        }
    }
}

fun interface EasingFunction {
    fun ease(double: Double): Double
    operator fun invoke(double: Double) = ease(double)
}
