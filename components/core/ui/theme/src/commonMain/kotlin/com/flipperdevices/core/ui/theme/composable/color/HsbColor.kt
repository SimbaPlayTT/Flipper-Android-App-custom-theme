package com.flipperdevices.core.ui.theme.composable.color

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

private const val HUE_DEGREES = 360f
private const val HUE_SEGMENT = 60f

@Suppress("MagicNumber")
fun hsbToColor(hue: Float, saturation: Float, brightness: Float): Color {
    val h = ((hue % HUE_DEGREES) + HUE_DEGREES) % HUE_DEGREES
    val s = saturation.coerceIn(0f, 1f)
    val v = brightness.coerceIn(0f, 1f)

    val c = v * s
    val x = c * (1 - abs((h / HUE_SEGMENT) % 2 - 1))
    val m = v - c

    val (r1, g1, b1) = when {
        h < HUE_SEGMENT -> Triple(c, x, 0f)
        h < 2 * HUE_SEGMENT -> Triple(x, c, 0f)
        h < 3 * HUE_SEGMENT -> Triple(0f, c, x)
        h < 4 * HUE_SEGMENT -> Triple(0f, x, c)
        h < 5 * HUE_SEGMENT -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(red = r1 + m, green = g1 + m, blue = b1 + m)
}

@Suppress("MagicNumber")
fun colorToHsb(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val hue = when {
        delta == 0f -> 0f
        max == r -> HUE_SEGMENT * (((g - b) / delta).mod(6f))
        max == g -> HUE_SEGMENT * (((b - r) / delta) + 2f)
        else -> HUE_SEGMENT * (((r - g) / delta) + 4f)
    }
    val saturation = if (max == 0f) 0f else delta / max
    val brightness = max

    return floatArrayOf(hue, saturation, brightness)
}

/** Returns a lighter/less saturated variant of [color], preserving hue. Used for "progress" tints. */
fun desaturate(color: Color, saturationFactor: Float): Color {
    val hsb = colorToHsb(color)
    return hsbToColor(hue = hsb[0], saturation = hsb[1] * saturationFactor, brightness = hsb[2])
}

private const val HEX_RADIX = 16
private const val HEX_LENGTH = 6
private const val BYTE_MASK = 0xFF
private const val CHANNEL_MAX = 255f

@Suppress("MagicNumber")
fun Color.toHexString(): String {
    val r = (red * CHANNEL_MAX).toInt().coerceIn(0, BYTE_MASK)
    val g = (green * CHANNEL_MAX).toInt().coerceIn(0, BYTE_MASK)
    val b = (blue * CHANNEL_MAX).toInt().coerceIn(0, BYTE_MASK)
    return listOf(r, g, b).joinToString(separator = "") { it.toString(HEX_RADIX).padStart(2, '0') }.uppercase()
}

/** Parses a bare "RRGGBB" hex string (no leading '#') into a [Color], or null if invalid. */
@Suppress("MagicNumber")
fun parseHexColor(hex: String): Color? {
    if (hex.length != HEX_LENGTH || hex.any { it !in "0123456789abcdefABCDEF" }) return null
    val value = hex.toLongOrNull(HEX_RADIX) ?: return null
    val r = ((value shr 16) and 0xFF).toInt()
    val g = ((value shr 8) and 0xFF).toInt()
    val b = (value and 0xFF).toInt()
    return Color(red = r / CHANNEL_MAX, green = g / CHANNEL_MAX, blue = b / CHANNEL_MAX)
}
