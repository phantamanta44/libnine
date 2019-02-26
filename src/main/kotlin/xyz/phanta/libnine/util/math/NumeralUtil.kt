package xyz.phanta.libnine.util.math

import kotlin.math.max
import kotlin.math.min

fun Int.clamp(lower: Int, upper: Int): Int = max(min(this, upper), lower)

