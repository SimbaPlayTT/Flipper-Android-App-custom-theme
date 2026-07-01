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
