package com.flipperdevices.screenstreaming.impl.composable.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flipperdevices.core.ui.theme.FlipperThemeInternal
import com.flipperdevices.core.ui.theme.LocalPallet
import com.flipperdevices.screenstreaming.impl.composable.ButtonEnum

private const val BUTTON_WEIGHT = 0.3f
private const val CONTROL_ICON_STROKE_WIDTH = 3f

private data class ControlIconSpec(
    val pathData: String,
    val viewportWidth: Float,
    val viewportHeight: Float
)

private val CONTROL_ICON_SPECS = mapOf(
    ButtonEnum.UP to ControlIconSpec(
        pathData = "M14.335,2.75C15.297,1.083 17.703,1.083 18.665,2.75L30.789,23.75C31.752,25.417 30.549,27.5 " +
            "28.624,27.5H4.376C2.451,27.5 1.248,25.417 2.211,23.75L14.335,2.75Z",
        viewportWidth = 33f,
        viewportHeight = 29f
    ),
    ButtonEnum.DOWN to ControlIconSpec(
        pathData = "M18.665,26.25C17.703,27.917 15.297,27.917 14.335,26.25L2.211,5.25C1.248,3.583 2.451,1.5 " +
            "4.376,1.5L28.624,1.5C30.549,1.5 31.752,3.583 30.789,5.25L18.665,26.25Z",
        viewportWidth = 33f,
        viewportHeight = 29f
    ),
    ButtonEnum.LEFT to ControlIconSpec(
        pathData = "M3.25,19.165C1.583,18.203 1.583,15.797 3.25,14.835L24.25,2.711C25.917,1.748 28,2.951 " +
            "28,4.876L28,29.124C28,31.049 25.917,32.252 24.25,31.289L3.25,19.165Z",
        viewportWidth = 30f,
        viewportHeight = 34f
    ),
    ButtonEnum.RIGHT to ControlIconSpec(
        pathData = "M26.75,14.835C28.417,15.797 28.417,18.203 26.75,19.165L5.75,31.289C4.083,32.252 2,31.049 " +
            "2,29.124L2,4.876C2,2.951 4.083,1.748 5.75,2.711L26.75,14.835Z",
        viewportWidth = 30f,
        viewportHeight = 34f
    ),
    ButtonEnum.OK to ControlIconSpec(
        pathData = "M24.5,24m-22.5,0a22.5,22.5 0,1 1,45 0a22.5,22.5 0,1 1,-45 0",
        viewportWidth = 49f,
        viewportHeight = 48f
    )
)

@Composable
private fun rememberTintedControlIcon(spec: ControlIconSpec, fillColor: Color): ImageVector {
    return remember(spec, fillColor) {
        ImageVector.Builder(
            defaultWidth = spec.viewportWidth.dp,
            defaultHeight = spec.viewportHeight.dp,
            viewportWidth = spec.viewportWidth,
            viewportHeight = spec.viewportHeight
        ).addPath(
            pathData = PathParser().parsePathString(spec.pathData).toNodes(),
            fill = SolidColor(fillColor),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = CONTROL_ICON_STROKE_WIDTH
        ).build()
    }
}

@Suppress("LongMethod")
@Composable
fun ComposableFlipperDPad(
    modifier: Modifier = Modifier,
    onPressButton: (ButtonEnum) -> Unit = {},
    onLongPressButton: (ButtonEnum) -> Unit = {}
) {
    /**
     * |------|  up  |------|
     * | left |  ok  | right |
     * |------| down  |-----|
     */
    Column(
        modifier
            .size(162.dp)
            .border(
                width = 3.dp,
                color = LocalPallet.current.screenStreamingBorderColor,
                shape = CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape)
            .background(LocalPallet.current.dPadAccent)
    ) {
        ControlRow(
            start = null,
            center = ButtonEnum.UP,
            end = null,
            onPressButton,
            onLongPressButton
        )
        ControlRow(
            start = ButtonEnum.LEFT,
            center = ButtonEnum.OK,
            end = ButtonEnum.RIGHT,
            onPressButton,
            onLongPressButton
        )
        ControlRow(
            start = null,
            center = ButtonEnum.DOWN,
            end = null,
            onPressButton,
            onLongPressButton
        )
    }
}

@Composable
private fun ColumnScope.ControlRow(
    start: ButtonEnum?,
    center: ButtonEnum?,
    end: ButtonEnum?,
    onPressButton: (ButtonEnum) -> Unit,
    onLongPressButton: (ButtonEnum) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .weight(BUTTON_WEIGHT)
            .fillMaxWidth()
    ) {
        ControlButton(
            modifier = Modifier.weight(BUTTON_WEIGHT),
            button = start,
            onPressButton = onPressButton,
            onLongPressButton = onLongPressButton
        )
        ControlButton(
            modifier = Modifier.weight(BUTTON_WEIGHT),
            button = center,
            onPressButton = onPressButton,
            onLongPressButton = onLongPressButton
        )
        ControlButton(
            modifier = Modifier.weight(BUTTON_WEIGHT),
            button = end,
            onPressButton = onPressButton,
            onLongPressButton = onLongPressButton
        )
    }
}

@Composable
@Suppress("ModifierReused")
private fun ControlButton(
    button: ButtonEnum?,
    onPressButton: (ButtonEnum) -> Unit,
    onLongPressButton: (ButtonEnum) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (button == null) {
        Box(modifier)
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onPressButton(button) },
                onLongClick = { onLongPressButton(button) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            val spec = CONTROL_ICON_SPECS[button]
            if (spec != null) {
                Image(
                    painter = rememberVectorPainter(
                        rememberTintedControlIcon(spec, LocalPallet.current.dPadAccent)
                    ),
                    contentDescription = stringResource(button.description)
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ComposableFlipperDPadPreview() {
    FlipperThemeInternal {
        ComposableFlipperDPad()
    }
}
