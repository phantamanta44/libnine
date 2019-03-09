package xyz.phanta.libnine.util.math

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

fun Int.clamp(lower: Int, upper: Int): Int = max(min(this, upper), lower)

fun Double.clamp(lower: Double, upper: Double): Double = max(min(this, upper), lower)

fun Double.randomNonZero(): Double = (this - 1).absoluteValue
