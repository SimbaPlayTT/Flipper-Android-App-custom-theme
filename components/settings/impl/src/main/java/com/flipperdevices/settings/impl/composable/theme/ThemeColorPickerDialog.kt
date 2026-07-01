package com.flipperdevices.settings.impl.composable.theme

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flipperdevices.core.ui.ktx.clickableRipple
import com.flipperdevices.core.ui.ktx.elements.ComposableFlipperButton
import com.flipperdevices.core.ui.theme.LocalPallet
import com.flipperdevices.core.ui.theme.LocalTypography
import com.flipperdevices.core.ui.theme.composable.color.hsbToColor
import com.flipperdevices.settings.impl.R
import com.flipperdevices.settings.impl.viewmodels.ThemeColorPickerViewModel

private val PRESET_HUES = listOf(0f, 30f, 60f, 90f, 120f, 150f, 180f, 210f, 240f, 270f, 300f, 330f)
private const val PRESET_COLUMNS = 6

@Composable
fun ThemeColorPickerDialog(
    viewModel: ThemeColorPickerViewModel,
    onDismiss: () -> Unit
) {
    val draft by viewModel.getDraftState().collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            CompositionLocalProvider(
                LocalPallet provides LocalPallet.current.copy(
                    accent = draft.accentColor,
                    dPadAccent = draft.dPadColor
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(LocalPallet.current.backgroundDialog)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.theme_picker_title),
                        style = LocalTypography.current.titleB18,
                        color = LocalPallet.current.text100
                    )

                    PresetGrid(onSelect = viewModel::selectPreset)

                    HueSlider(
                        titleRes = R.string.theme_picker_hue,
                        hue = draft.hue,
                        onHueChange = viewModel::updateHue,
                        onValueChangeFinished = viewModel::commitDraft
                    )

                    SaturationSlider(
                        hue = draft.hue,
                        saturation = draft.saturation,
                        onValueChange = viewModel::updateSaturation,
                        onValueChangeFinished = viewModel::commitDraft
                    )

                    BrightnessSlider(
                        hue = draft.hue,
                        saturation = draft.saturation,
                        brightness = draft.brightness,
                        onValueChange = viewModel::updateBrightness,
                        onValueChangeFinished = viewModel::commitDraft
                    )

                    HueSlider(
                        titleRes = R.string.theme_picker_dpad,
                        hue = draft.dPadHue,
                        onHueChange = viewModel::updateDPadHue,
                        onValueChangeFinished = viewModel::commitDraft
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .clickableRipple { viewModel.resetToDefault() }
                                .padding(vertical = 14.dp),
                            text = stringResource(R.string.theme_picker_reset),
                            textAlign = TextAlign.Center,
                            color = LocalPallet.current.text60,
                            style = LocalTypography.current.buttonM16
                        )
                        ComposableFlipperButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.theme_picker_done),
                            onClick = {
                                viewModel.commitDraft()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetGrid(onSelect: (Color) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PRESET_HUES.chunked(PRESET_COLUMNS).forEach { rowHues ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowHues.forEach { hue ->
                    val color = hsbToColor(hue, 1f, 1f)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color)
                            .clickableRipple { onSelect(color) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SliderRow(
    @StringRes titleRes: Int,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(titleRes),
            style = LocalTypography.current.subtitleB12,
            color = LocalPallet.current.text60
        )
        content()
    }
}

@Composable
private fun HueSlider(
    @StringRes titleRes: Int,
    hue: Float,
    onHueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val hueStops = remember { (0..12).map { hsbToColor(it * 30f, 1f, 1f) } }
    SliderRow(titleRes = titleRes) {
        GradientSlider(
            value = hue,
            onValueChange = onHueChange,
            valueRange = 0f..360f,
            brush = Brush.horizontalGradient(hueStops),
            onValueChangeFinished = onValueChangeFinished
        )
    }
}

@Composable
private fun SaturationSlider(
    hue: Float,
    saturation: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val brush = remember(hue) {
        Brush.horizontalGradient(listOf(hsbToColor(hue, 0f, 1f), hsbToColor(hue, 1f, 1f)))
    }
    SliderRow(titleRes = R.string.theme_picker_saturation) {
        GradientSlider(
            value = saturation,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            brush = brush,
            onValueChangeFinished = onValueChangeFinished
        )
    }
}

@Composable
private fun BrightnessSlider(
    hue: Float,
    saturation: Float,
    brightness: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    val brush = remember(hue, saturation) {
        Brush.horizontalGradient(listOf(Color.Black, hsbToColor(hue, saturation, 1f)))
    }
    SliderRow(titleRes = R.string.theme_picker_brightness) {
        GradientSlider(
            value = brightness,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            brush = brush,
            onValueChangeFinished = onValueChangeFinished
        )
    }
}

@Composable
private fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    brush: Brush,
    onValueChangeFinished: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}
