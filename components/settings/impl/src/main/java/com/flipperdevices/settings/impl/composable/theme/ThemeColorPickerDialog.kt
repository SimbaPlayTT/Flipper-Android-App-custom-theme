package com.flipperdevices.settings.impl.composable.theme

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flipperdevices.core.preference.pb.SpoofShellColor
import com.flipperdevices.core.ui.ktx.clickableRipple
import com.flipperdevices.core.ui.ktx.elements.ComposableFlipperButton
import com.flipperdevices.core.ui.theme.LocalPallet
import com.flipperdevices.core.ui.theme.LocalTypography
import com.flipperdevices.core.ui.theme.composable.color.hsbToColor
import com.flipperdevices.core.ui.theme.composable.color.toHexString
import com.flipperdevices.settings.impl.R
import com.flipperdevices.settings.impl.viewmodels.MIN_ACCENT_BRIGHTNESS
import com.flipperdevices.settings.impl.viewmodels.SpoofShellState
import com.flipperdevices.settings.impl.viewmodels.ThemeColorPickerViewModel
import com.flipperdevices.settings.impl.viewmodels.ThemeDraftState

private val PRESET_HUES = listOf(0f, 30f, 60f, 90f, 120f, 150f, 180f, 210f, 240f, 270f, 300f, 330f)
private const val PRESET_COLUMNS = 6
private const val HEX_LENGTH = 6

@Composable
fun ThemeColorPickerDialog(
    viewModel: ThemeColorPickerViewModel,
    onDismiss: () -> Unit
) {
    val draft by viewModel.getDraftState().collectAsState()
    val spoofShell by viewModel.getSpoofShellState().collectAsState()
    var showCameraPicker by remember { mutableStateOf(false) }

    if (showCameraPicker) {
        CameraColorPickerDialog(
            onColorPicked = viewModel::applyCameraColor,
            onDismiss = { showCameraPicker = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
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

                LivePreviewRow(
                    draft = draft,
                    onHexSubmit = viewModel::applyHex,
                    onOpenCamera = { showCameraPicker = true }
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

                SpoofShellRow(state = spoofShell, onSelect = viewModel::setSpoofShellColor)

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

/**
 * A small live-updating color swatch + hex input, computed straight from [draft] rather than by
 * overriding LocalPallet - overriding LocalPallet for the whole dialog previously forced the
 * *entire* dialog subtree to recompose on every drag pixel even though nothing in the dialog
 * actually reads accent through it, which is what made the sliders feel laggy/unresponsive.
 */
@Composable
private fun LivePreviewRow(
    draft: ThemeDraftState,
    onHexSubmit: (String) -> Boolean,
    onOpenCamera: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var hexText by remember { mutableStateOf(draft.accentColor.toHexString()) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(draft.accentColor, isFocused) {
        if (!isFocused) {
            hexText = draft.accentColor.toHexString()
            isError = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(draft.accentColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LocalPallet.current.hexKeyboardBackground)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#",
                    style = LocalTypography.current.bodyR16,
                    color = LocalPallet.current.text60
                )
                BasicTextField(
                    value = hexText,
                    onValueChange = { input ->
                        hexText = input.uppercase().filter { it in "0123456789ABCDEF" }.take(HEX_LENGTH)
                        isError = false
                    },
                    modifier = Modifier.padding(start = 4.dp),
                    singleLine = true,
                    textStyle = LocalTypography.current.bodyR16.copy(color = LocalPallet.current.text100),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        isError = !onHexSubmit(hexText)
                    }),
                    cursorBrush = SolidColor(LocalPallet.current.text100),
                    interactionSource = interactionSource
                )
            }
            if (isError) {
                Text(
                    text = stringResource(R.string.theme_picker_hex_error),
                    style = LocalTypography.current.subtitleB12,
                    color = LocalPallet.current.onError
                )
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LocalPallet.current.hexKeyboardBackground)
                .clickableRipple(onClick = onOpenCamera),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.theme_picker_camera),
                style = LocalTypography.current.subtitleB12,
                color = LocalPallet.current.text60,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Forces the Device Info mockup to a specific shell color regardless of what the connected
 * Flipper actually reports - a manual fallback for firmwares (e.g. Momentum) whose spoofed color
 * either isn't exposed over the standard hardware.color RPC property, or where the user simply
 * wants the in-app mockup to look different from the real hardware.
 */
@Composable
private fun SpoofShellRow(
    state: SpoofShellState,
    onSelect: (SpoofShellColor?) -> Unit
) {
    SliderRow(titleRes = R.string.theme_picker_spoof_shell) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpoofShellChip(
                modifier = Modifier.weight(1f),
                labelRes = R.string.theme_picker_spoof_auto,
                swatch = null,
                selected = state.color == null,
                onClick = { onSelect(null) }
            )
            SpoofShellChip(
                modifier = Modifier.weight(1f),
                labelRes = R.string.theme_picker_spoof_white,
                swatch = Color.White,
                selected = state.color == SpoofShellColor.SPOOF_WHITE,
                onClick = { onSelect(SpoofShellColor.SPOOF_WHITE) }
            )
            SpoofShellChip(
                modifier = Modifier.weight(1f),
                labelRes = R.string.theme_picker_spoof_black,
                swatch = Color.Black,
                selected = state.color == SpoofShellColor.SPOOF_BLACK,
                onClick = { onSelect(SpoofShellColor.SPOOF_BLACK) }
            )
            SpoofShellChip(
                modifier = Modifier.weight(1f),
                labelRes = R.string.theme_picker_spoof_transparent,
                swatch = Color.White.copy(alpha = 0.2f),
                selected = state.color == SpoofShellColor.SPOOF_TRANSPARENT,
                onClick = { onSelect(SpoofShellColor.SPOOF_TRANSPARENT) }
            )
        }
    }
}

@Composable
private fun SpoofShellChip(
    @StringRes labelRes: Int,
    swatch: Color?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) LocalPallet.current.accent else LocalPallet.current.text16
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .clickableRipple(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (swatch != null) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(width = 1.dp, color = LocalPallet.current.text16, shape = RoundedCornerShape(4.dp))
                    .background(swatch)
            )
        } else {
            Box(modifier = Modifier.size(16.dp))
        }
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = stringResource(labelRes),
            style = LocalTypography.current.subtitleB12,
            color = LocalPallet.current.text60
        )
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
    // Floored at MIN_ACCENT_BRIGHTNESS: a brightness of 0 always renders black regardless of
    // hue/saturation, and accent is used as both the D-Pad's fill AND its background, so a
    // black accent makes the D-Pad (and its icons) disappear entirely.
    val brush = remember(hue, saturation) {
        Brush.horizontalGradient(
            listOf(hsbToColor(hue, saturation, MIN_ACCENT_BRIGHTNESS), hsbToColor(hue, saturation, 1f))
        )
    }
    SliderRow(titleRes = R.string.theme_picker_brightness) {
        GradientSlider(
            value = brightness,
            onValueChange = onValueChange,
            valueRange = MIN_ACCENT_BRIGHTNESS..1f,
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
