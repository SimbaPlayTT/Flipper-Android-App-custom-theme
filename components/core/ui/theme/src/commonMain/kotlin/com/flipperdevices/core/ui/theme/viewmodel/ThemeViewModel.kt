package com.flipperdevices.core.ui.theme.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import com.flipperdevices.core.preference.pb.SelectedTheme
import com.flipperdevices.core.preference.pb.Settings
import com.flipperdevices.core.ui.lifecycle.DecomposeViewModel
import com.flipperdevices.core.ui.theme.composable.color.hsbToColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@Immutable
class ThemeViewModel @Inject constructor(
    private val dataStoreSettings: DataStore<Settings>
) : DecomposeViewModel() {
    private val selectedThemeStateFlow = MutableStateFlow<SelectedTheme>(SelectedTheme.SYSTEM)
    private val accentStateFlow = MutableStateFlow<Color?>(null)
    private val dPadAccentStateFlow = MutableStateFlow<Color?>(null)

    init {
        dataStoreSettings.data.onEach { settings ->
            selectedThemeStateFlow.emit(settings.selected_theme)
            if (settings.custom_accent_theme_enabled) {
                accentStateFlow.emit(
                    hsbToColor(
                        hue = settings.custom_accent_hue,
                        saturation = settings.custom_accent_saturation,
                        brightness = settings.custom_accent_brightness
                    )
                )
                dPadAccentStateFlow.emit(
                    hsbToColor(
                        hue = settings.custom_dpad_accent_hue,
                        saturation = settings.custom_accent_saturation,
                        brightness = settings.custom_accent_brightness
                    )
                )
            } else {
                accentStateFlow.emit(null)
                dPadAccentStateFlow.emit(null)
            }
        }.launchIn(viewModelScope)
    }

    fun getAppTheme() = selectedThemeStateFlow.asStateFlow()
    fun getCustomAccentColor() = accentStateFlow.asStateFlow()
    fun getCustomDPadAccentColor() = dPadAccentStateFlow.asStateFlow()
}
