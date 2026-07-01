package com.flipperdevices.screenstreaming.impl.composable.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flipperdevices.core.ui.theme.FlipperThemeInternal
import com.flipperdevices.core.ui.theme.LocalPallet
import com.flipperdevices.screenstreaming.impl.model.ButtonAnimEnum
import com.flipperdevices.screenstreaming.impl.model.FlipperButtonStackElement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * These icons used to be recolored with ColorFilter.tint(accent, BlendMode.Color). That leaks:
 * a blend-mode color filter composites its (opaque) tint color with standard alpha-over math, so
 * wherever the source bitmap had alpha=0 (the transparent space around each icon) the result
 * became a fully opaque accent-colored square instead of staying transparent - that was the
 * reported "glitch" (solid colored blocks instead of transparent icon backgrounds). Rebuilding
 * each icon layer-by-layer (same technique as the D-Pad icons in ComposableFlipperDPad.kt)
 * sidesteps this entirely: every layer's alpha is exact and nothing outside the drawn paths is
 * ever painted.
 */
private enum class LayerColor { ACCENT, BLACK, NONE }

private data class IconLayer(
    val pathData: String,
    val fillType: PathFillType = PathFillType.NonZero,
    val fill: LayerColor = LayerColor.NONE,
    val fillAlpha: Float = 1f,
    val stroke: LayerColor = LayerColor.NONE,
    val strokeAlpha: Float = 1f,
    val strokeWidth: Float = 0f
)

private data class AnimIconSpec(val viewportSize: Float, val layers: List<IconLayer>)

private const val CIRCLE_24_OUTER = "M12,12m-9.75,0a9.75,9.75 0,1 1,19.5 0a9.75,9.75 0,1 1,-19.5 0"
private const val CIRCLE_24_INNER = "M12,12m-9,0a9,9 0,1 1,18 0a9,9 0,1 1,-18 0"

private const val LEFT_PATH = "M14.625,4.422L4.125,10.484C2.958,11.158 2.958,12.842 4.125,13.516L14.625," +
    "19.578C15.792,20.251 17.25,19.409 17.25,18.062V5.938C17.25,4.591 15.792,3.749 14.625,4.422Z"
private const val LEFT_PATH_INNER = "M4.5,12.866C3.833,12.481 3.833,11.519 4.5,11.134L15,5.072C15.667," +
    "4.687 16.5,5.168 16.5,5.938V18.062C16.5,18.832 15.667,19.313 15,18.928L4.5,12.866Z"

private const val RIGHT_PATH = "M9.375,19.578L19.875,13.516C21.042,12.842 21.042,11.158 19.875,10.484L" +
    "9.375,4.422C8.208,3.749 6.75,4.591 6.75,5.938L6.75,18.062C6.75,19.409 8.208,20.251 9.375,19.578Z"
private const val RIGHT_PATH_INNER = "M19.5,11.134C20.167,11.519 20.167,12.481 19.5,12.866L9,18.928C" +
    "8.333,19.313 7.5,18.832 7.5,18.062L7.5,5.938C7.5,5.168 8.333,4.687 9,5.072L19.5,11.134Z"

private const val UP_PATH = "M19.578,14.625L13.516,4.125C12.842,2.958 11.158,2.958 10.484,4.125L4.422," +
    "14.625C3.749,15.792 4.591,17.25 5.938,17.25H18.062C19.409,17.25 20.251,15.792 19.578,14.625Z"
private const val UP_PATH_INNER = "M11.134,4.5C11.519,3.833 12.481,3.833 12.866,4.5L18.928,15C19.313," +
    "15.667 18.832,16.5 18.062,16.5H5.938C5.168,16.5 4.687,15.667 5.072,15L11.134,4.5Z"

private const val DOWN_PATH = "M4.422,9.375L10.484,19.875C11.158,21.042 12.842,21.042 13.516,19.875L" +
    "19.578,9.375C20.251,8.208 19.409,6.75 18.062,6.75L5.938,6.75C4.591,6.75 3.749,8.208 4.422,9.375Z"
private const val DOWN_PATH_INNER = "M12.866,19.5C12.481,20.167 11.519,20.167 11.134,19.5L5.072,9C4.687," +
    "8.333 5.168,7.5 5.938,7.5L18.062,7.5C18.832,7.5 19.313,8.333 18.928,9L12.866,19.5Z"

private const val BACK_GLYPH = "M14.225,16.197H9.397V14.371H13.971C14.733,14.371 15.242,13.397 " +
    "15.242,12.788C15.242,12.545 15.115,12.058 14.988,11.693C14.733,11.327 14.479,11.084 " +
    "14.098,11.084H10.032V12.301L6.602,10.11L10.032,7.797V9.136H13.971C14.86,9.136 15.75,9.623 " +
    "16.512,10.353C17.147,11.084 17.402,11.936 17.402,12.666C17.402,13.64 17.147,14.493 " +
    "16.512,15.101C15.877,15.832 15.115,16.197 14.225,16.197ZM10.159,15.466H14.225C14.86,15.466 " +
    "15.496,15.223 16.004,14.614C16.512,14.127 16.766,13.397 16.766,12.545C16.766,11.936 " +
    "16.512,11.206 16.004,10.597C15.496,9.988 14.86,9.623 14.098,9.623H9.524V8.893L8.126,9.866L" +
    "9.524,10.719V10.11H14.225C14.86,10.11 15.496,10.475 15.75,11.084C16.131,11.693 16.131," +
    "12.301 16.131,12.545C16.131,13.519 15.369,14.736 14.225,14.736H10.286L10.159,15.466Z"

private const val UNLOCK_CIRCLE_OUTER = "M11,11m-9.75,0a9.75,9.75 0,1 1,19.5 0a9.75,9.75 0,1 1,-19.5 0"
private const val UNLOCK_CIRCLE_INNER = "M11,11m-9,0a9,9 0,1 1,18 0a9,9 0,1 1,-18 0"
private const val UNLOCK_BODY = "M7.325,10.91C7.035,10.91 6.8,11.151 6.8,11.449V15.218C6.8,15.515 " +
    "7.035,15.756 7.325,15.756H14.675C14.965,15.756 15.2,15.515 15.2,15.218V11.449C15.2,11.151 " +
    "14.965,10.91 14.675,10.91H7.325ZM5.75,11.449C5.75,10.557 6.455,9.833 7.325,9.833H14.675C" +
    "15.545,9.833 16.25,10.557 16.25,11.449V15.218C16.25,16.11 15.545,16.833 14.675,16.833H7.325C" +
    "6.455,16.833 5.75,16.11 5.75,15.218V11.449Z"
private const val UNLOCK_SHACKLE = "M8.345,10.417C8.049,10.417 7.806,10.202 7.806,9.94V8.033C" +
    "7.806,7.322 8.103,6.64 8.642,6.111C9.181,5.582 9.92,5.253 10.718,5.181C11.516,5.11 " +
    "12.314,5.305 12.961,5.725C13.608,6.144 14.05,6.764 14.212,7.46C14.271,7.718 14.083,7.971 " +
    "13.791,8.023C13.5,8.076 13.215,7.909 13.155,7.651C13.047,7.189 12.751,6.779 12.319,6.492C" +
    "11.888,6.211 11.354,6.082 10.826,6.13C10.292,6.178 9.801,6.397 9.44,6.75C9.079,7.103 " +
    "8.885,7.561 8.885,8.033V9.94C8.885,10.207 8.642,10.417 8.345,10.417Z"

private const val ANIM_ICON_STROKE_WIDTH = 1.5f
private const val ANIM_ICON_FILL_ALPHA = 0.5f
private const val ANIM_ICON_RING_ALPHA = 0.25f

private fun arrowSpec(outer: String, inner: String): Pair<AnimIconSpec, AnimIconSpec> {
    val light = AnimIconSpec(
        viewportSize = 24f,
        layers = listOf(
            IconLayer(
                pathData = outer,
                fill = LayerColor.ACCENT,
                fillAlpha = ANIM_ICON_FILL_ALPHA,
                stroke = LayerColor.BLACK,
                strokeAlpha = ANIM_ICON_FILL_ALPHA,
                strokeWidth = ANIM_ICON_STROKE_WIDTH
            )
        )
    )
    val dark = AnimIconSpec(
        viewportSize = 24f,
        layers = listOf(
            IconLayer(pathData = inner, fill = LayerColor.ACCENT, fillAlpha = ANIM_ICON_FILL_ALPHA),
            IconLayer(
                pathData = outer,
                stroke = LayerColor.ACCENT,
                strokeAlpha = ANIM_ICON_RING_ALPHA,
                strokeWidth = ANIM_ICON_STROKE_WIDTH
            )
        )
    )
    return light to dark
}

private fun circleSpec(
    glyphLayers: List<IconLayer>,
    viewportSize: Float,
    outer: String,
    inner: String
): Pair<AnimIconSpec, AnimIconSpec> {
    val light = AnimIconSpec(
        viewportSize = viewportSize,
        layers = listOf(
            IconLayer(
                pathData = outer,
                fill = LayerColor.ACCENT,
                fillAlpha = ANIM_ICON_FILL_ALPHA,
                stroke = LayerColor.BLACK,
                strokeAlpha = ANIM_ICON_FILL_ALPHA,
                strokeWidth = ANIM_ICON_STROKE_WIDTH
            )
        ) + glyphLayers
    )
    val dark = AnimIconSpec(
        viewportSize = viewportSize,
        layers = listOf(
            IconLayer(pathData = inner, fill = LayerColor.ACCENT, fillAlpha = ANIM_ICON_FILL_ALPHA),
            IconLayer(
                pathData = outer,
                stroke = LayerColor.ACCENT,
                strokeAlpha = ANIM_ICON_RING_ALPHA,
                strokeWidth = ANIM_ICON_STROKE_WIDTH
            )
        ) + glyphLayers
    )
    return light to dark
}

private val ANIM_ICON_SPECS: Map<ButtonAnimEnum, Pair<AnimIconSpec, AnimIconSpec>> = mapOf(
    ButtonAnimEnum.LEFT to arrowSpec(LEFT_PATH, LEFT_PATH_INNER),
    ButtonAnimEnum.RIGHT to arrowSpec(RIGHT_PATH, RIGHT_PATH_INNER),
    ButtonAnimEnum.UP to arrowSpec(UP_PATH, UP_PATH_INNER),
    ButtonAnimEnum.DOWN to arrowSpec(DOWN_PATH, DOWN_PATH_INNER),
    ButtonAnimEnum.OK to circleSpec(emptyList(), 24f, CIRCLE_24_OUTER, CIRCLE_24_INNER),
    ButtonAnimEnum.BACK to circleSpec(
        glyphLayers = listOf(
            IconLayer(pathData = BACK_GLYPH, fill = LayerColor.BLACK, fillAlpha = ANIM_ICON_FILL_ALPHA)
        ),
        viewportSize = 24f,
        outer = CIRCLE_24_OUTER,
        inner = CIRCLE_24_INNER
    ),
    ButtonAnimEnum.UNLOCK to circleSpec(
        glyphLayers = listOf(
            IconLayer(
                pathData = UNLOCK_BODY,
                fillType = PathFillType.EvenOdd,
                fill = LayerColor.BLACK,
                fillAlpha = ANIM_ICON_FILL_ALPHA
            ),
            IconLayer(pathData = UNLOCK_SHACKLE, fill = LayerColor.BLACK, fillAlpha = ANIM_ICON_FILL_ALPHA)
        ),
        viewportSize = 22f,
        outer = UNLOCK_CIRCLE_OUTER,
        inner = UNLOCK_CIRCLE_INNER
    )
)

private fun LayerColor.toBrush(accent: Color): SolidColor? = when (this) {
    LayerColor.ACCENT -> SolidColor(accent)
    LayerColor.BLACK -> SolidColor(Color.Black)
    LayerColor.NONE -> null
}

@Composable
private fun rememberTintedAnimIcon(spec: AnimIconSpec, accent: Color): ImageVector {
    return remember(spec, accent) {
        ImageVector.Builder(
            defaultWidth = spec.viewportSize.dp,
            defaultHeight = spec.viewportSize.dp,
            viewportWidth = spec.viewportSize,
            viewportHeight = spec.viewportSize
        ).apply {
            spec.layers.forEach { layer ->
                addPath(
                    pathData = PathParser().parsePathString(layer.pathData).toNodes(),
                    pathFillType = layer.fillType,
                    fill = layer.fill.toBrush(accent),
                    fillAlpha = layer.fillAlpha,
                    stroke = layer.stroke.toBrush(accent),
                    strokeAlpha = layer.strokeAlpha,
                    strokeLineWidth = layer.strokeWidth
                )
            }
        }.build()
    }
}

@Composable
internal fun ComposableFlipperButtonAnimation(
    buttons: ImmutableList<FlipperButtonStackElement>
) {
    val accent = LocalPallet.current.accent
    val isLight = MaterialTheme.colors.isLight

    LazyRow(
        modifier = Modifier
            .padding(bottom = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        userScrollEnabled = false
    ) {
        items(
            count = buttons.size,
            key = { buttons[it].uuid }
        ) {
            val button = buttons[it]
            val specs = ANIM_ICON_SPECS.getValue(button.enum)
            val spec = if (isLight) specs.first else specs.second
            Image(
                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    .size(24.dp),
                imageVector = rememberTintedAnimIcon(spec, accent),
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
private fun ComposableFlipperButtonPreview() {
    val buttons = remember {
        mutableListOf(
            FlipperButtonStackElement(ButtonAnimEnum.BACK),
        ).toMutableStateList()
    }
    FlipperThemeInternal {
        Column(Modifier.fillMaxSize()) {
            ComposableFlipperButtonAnimation(buttons.toImmutableList())
            Button(onClick = {
                buttons.add(FlipperButtonStackElement(ButtonAnimEnum.BACK))
            }) {
                Text(text = "New button")
            }
            Button(onClick = {
                buttons.removeAt(0)
            }) {
                Text(text = "Delete")
            }
        }
    }
}
