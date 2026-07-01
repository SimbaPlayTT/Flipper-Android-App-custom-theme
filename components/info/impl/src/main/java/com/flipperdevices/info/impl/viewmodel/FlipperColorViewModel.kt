package com.flipperdevices.info.impl.viewmodel

import androidx.datastore.core.DataStore
import com.flipperdevices.bridge.connection.config.api.FDevicePersistedStorage
import com.flipperdevices.bridge.connection.config.api.model.FDeviceFlipperZeroBleModel
import com.flipperdevices.bridge.connection.feature.devicecolor.api.FDeviceColorFeatureApi
import com.flipperdevices.bridge.connection.feature.provider.api.FFeatureProvider
import com.flipperdevices.bridge.connection.feature.provider.api.FFeatureStatus
import com.flipperdevices.bridge.connection.feature.provider.api.get
import com.flipperdevices.core.preference.pb.FlipperZeroBle.HardwareColor
import com.flipperdevices.core.preference.pb.Settings
import com.flipperdevices.core.preference.pb.SpoofShellColor
import com.flipperdevices.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class FlipperColorViewModel @Inject constructor(
    fDevicePersistedStorage: FDevicePersistedStorage,
    dataStoreSettings: DataStore<Settings>,
    private val fFeatureProvider: FFeatureProvider
) : DecomposeViewModel() {
    private val realColorState = fDevicePersistedStorage.getCurrentDevice()
        .filterIsInstance<FDeviceFlipperZeroBleModel>()
        .map { coloredDevice -> coloredDevice.hardwareColor }

    /**
     * A manual override for whenever the live RPC refresh below can't pick up a spoofed color on
     * its own (e.g. a firmware that spoofs the name/color over BLE advertising but answers the
     * hardware.color RPC property honestly) - lets the user force the mockup to a specific shell
     * regardless of what the connected device actually reports.
     */
    private val colorFlipperState = combine(
        realColorState,
        dataStoreSettings.data
    ) { realColor, settings ->
        if (settings.spoof_shell_enabled) {
            when (settings.spoof_shell_color) {
                SpoofShellColor.SPOOF_BLACK -> HardwareColor.BLACK
                SpoofShellColor.SPOOF_TRANSPARENT -> HardwareColor.TRANSPARENT
                else -> HardwareColor.WHITE
            }
        } else {
            realColor
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HardwareColor.fromValue(-1))

    fun getFlipperColor(): StateFlow<HardwareColor> = colorFlipperState

    init {
        refreshLiveColor()
    }

    /**
     * The color captured at pairing time comes from a one-off passive BLE advertisement scan and
     * never updates again on its own. If the paired Flipper's reported color changes later (a
     * shell swap, or a color spoofed by custom firmware such as Momentum), the mockup would show
     * a stale value forever. Re-querying hardware.color over RPC whenever this screen is visited
     * (and persisting the result, same as pairing does) keeps it honest - and picks up whatever
     * the connected firmware currently reports, spoofed or not.
     */
    private fun refreshLiveColor() {
        fFeatureProvider.get<FDeviceColorFeatureApi>()
            .map { status -> status as? FFeatureStatus.Supported<FDeviceColorFeatureApi> }
            .flatMapLatest { status -> status?.featureApi?.updateAndGetColorFlow() ?: emptyFlow() }
            .launchIn(viewModelScope)
    }
}
