package com.flipperdevices.settings.impl.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import com.flipperdevices.core.preference.pb.Settings
import com.flipperdevices.core.ui.lifecycle.DecomposeViewModel
import com.flipperdevices.core.ui.theme.composable.color.colorToHsb
import com.flipperdevices.core.ui.theme.composable.color.hsbToColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemeDraftState(
    val hue: Float,
    val saturation: Float,
    val brightness: Float,
    val dPadHue: Float
) {
    val accentColor: Color get() = hsbToColor(hue, saturation, brightness)
    val dPadColor: Color get() = hsbToColor(dPadHue, saturation, brightness)
}

class ThemeColorPickerViewModel @Inject constructor(
    private val dataStoreSettings: DataStore<Settings>
) : DecomposeViewModel() {
    private val draftStateFlow = MutableStateFlow(defaultDraftState())

    init {
        viewModelScope.launch {
            val settings = dataStoreSettings.data.first()
            if (settings.custom_accent_theme_enabled) {
                draftStateFlow.value = ThemeDraftState(
                    hue = settings.custom_accent_hue,
                    saturation = settings.custom_accent_saturation,
                    brightness = settings.custom_accent_brightness,
                    dPadHue = settings.custom_dpad_accent_hue
                )
            }
        }
    }

    fun getDraftState(): StateFlow<ThemeDraftState> = draftStateFlow.asStateFlow()

    fun updateHue(hue: Float) {
        draftStateFlow.update { it.copy(hue = hue) }
    }

    fun updateSaturation(saturation: Float) {
        draftStateFlow.update { it.copy(saturation = saturation) }
    }

    fun updateBrightness(brightness: Float) {
        draftStateFlow.update { it.copy(brightness = brightness) }
    }

    fun updateDPadHue(dPadHue: Float) {
        draftStateFlow.update { it.copy(dPadHue = dPadHue) }
    }

    fun selectPreset(color: Color) {
        val hsb = colorToHsb(color)
        draftStateFlow.value = ThemeDraftState(
            hue = hsb[0],
            saturation = hsb[1],
            brightness = hsb[2],
            dPadHue = hsb[0]
        )
        commitDraft()
    }

    fun commitDraft() {
        val draft = draftStateFlow.value
        viewModelScope.launch {
            dataStoreSettings.updateData {
                it.copy(
                    custom_accent_theme_enabled = true,
                    custom_accent_hue = draft.hue,
                    custom_accent_saturation = draft.saturation,
                    custom_accent_brightness = draft.brightness,
                    custom_dpad_accent_hue = draft.dPadHue
                )
            }
        }
    }

    fun resetToDefault() {
        draftStateFlow.value = defaultDraftState()
        viewModelScope.launch {
            dataStoreSettings.updateData {
                it.copy(custom_accent_theme_enabled = false)
            }
        }
    }

    companion object {
        @Suppress("MagicNumber")
        private val DEFAULT_ACCENT = Color(0xFFFF8200)

        private fun defaultDraftState(): ThemeDraftState {
            val hsb = colorToHsb(DEFAULT_ACCENT)
            return ThemeDraftState(hue = hsb[0], saturation = hsb[1], brightness = hsb[2], dPadHue = hsb[0])
        }
    }
}
