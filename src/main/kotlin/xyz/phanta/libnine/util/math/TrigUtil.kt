package xyz.phanta.libnine.util.math

import kotlin.math.PI

const val TWO_PI: Double = PI * 2
const val PI_F: Float = 3.14159265358979F
const val TWO_PI_F: Float = PI_F * 2

fun Float.degToRad(): Float = this * PI_F / 180F
fun Float.radToDeg(): Float = this * 180F / PI_F

fun Double.degToRad(): Double = this * PI / 180.0
fun Double.radToDeg(): Double = this * 180.0 / PI
