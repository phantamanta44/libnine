package xyz.phanta.libnine.util.format

import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val CAMEL_PATTERN: Regex = Regex("""([a-z])([A-Z])""")

fun String.snakeify(): String = this.decapitalize().replace(CAMEL_PATTERN) { "${it.groupValues[1]}_${it.groupValues[2].toLowerCase()}" }

private val SI_PREFIXES: Array<String?> = arrayOf(null, "k", "M", "G", "T", "P", "E")
private val SI_PREFIXES_FP: Array<String> = arrayOf("n", "\u03bc", "m", "", "k", "M", "G", "T", "P", "E")

fun Int.formatSi(unit: String): String {
    if (this == 0) return "0 $unit"
    val magnitude = floor(log10(this.absoluteValue.toDouble()) / 3).toInt()
    return if (magnitude == 0) {
        "$this $unit"
    } else {
        "%.2f %s%s".format(this / 10.0.pow(magnitude * 3), SI_PREFIXES[magnitude], unit)
    }
}

fun Long.formatSi(unit: String): String {
    if (this == 0L) return "0 $unit"
    val magnitude = floor(log10(this.absoluteValue.toDouble()) / 3).toInt()
    return if (magnitude == 0) {
        "$this $unit"
    } else {
        "%.2f %s%s".format(this / 10.0.pow(magnitude * 3), SI_PREFIXES[magnitude], unit)
    }
}

fun Double.formatSi(unit: String): String {
    if (this == 0.0) return "0 $unit"
    val scaled = this * 1e9
    val magnitude = floor(log10(scaled.absoluteValue) / 3).toInt()
    return "%.2f %s%s".format(scaled / 10.0.pow(magnitude * 3), SI_PREFIXES_FP[magnitude], unit)
}

fun Float.formatPercent(): String = "%.1f%%".format(this * 100)

fun Double.formatPercent(): String = "%.1f%%".format(this * 100)

fun Class<*>.getReadableName(): String = this.name.let { it.substring(it.lastIndexOf(".") + 1) }.replace('$', '_')
