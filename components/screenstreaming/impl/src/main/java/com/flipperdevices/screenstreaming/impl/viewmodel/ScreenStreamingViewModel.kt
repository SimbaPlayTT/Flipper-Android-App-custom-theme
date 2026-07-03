package com.flipperdevices.screenstreaming.impl.viewmodel

import android.app.Application
import android.os.Vibrator
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.flipperdevices.bridge.connection.feature.provider.api.FFeatureProvider
import com.flipperdevices.core.ktx.android.vibrateCompat
import com.flipperdevices.core.preference.pb.Settings
import com.flipperdevices.core.ui.lifecycle.DecomposeViewModel
import com.flipperdevices.protobuf.screen.InputType
import com.flipperdevices.screenstreaming.impl.composable.ButtonEnum
import com.flipperdevices.screenstreaming.impl.model.FlipperScreenState
import com.flipperdevices.screenstreaming.impl.viewmodel.repository.ButtonStackRepository
import com.flipperdevices.screenstreaming.impl.viewmodel.repository.FlipperButtonRepository
import com.flipperdevices.screenstreaming.impl.viewmodel.repository.LockRepository
import com.flipperdevices.screenstreaming.impl.viewmodel.repository.StreamingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val VIBRATOR_TIME_MS = 10L

class ScreenStreamingViewModel @AssistedInject constructor(
    @Assisted private val lifecycleOwner: LifecycleOwner,
    application: Application,
    private val flipperButtonRepository: FlipperButtonRepository,
    private val buttonStackRepository: ButtonStackRepository,
    private val settings: DataStore<Settings>,
    private val fFeatureProvider: FFeatureProvider
) : DecomposeViewModel() {
    private val vibrator = ContextCompat.getSystemService(application, Vibrator::class.java)

    private val lockRepository = LockRepository(
        scope = viewModelScope,
        stackRepository = buttonStackRepository,
        fFeatureProvider = fFeatureProvider
    )
    private val streamingRepository = StreamingRepository(
        scope = viewModelScope,
        fFeatureProvider = fFeatureProvider,
        settings = settings
    )

    /**
     * Button presses are sent from Compose's onClick, i.e. the main thread. Reading this via
     * `runBlocking { settings.data.first() }` on every single press used to block the UI thread
     * on a DataStore disk read before the RPC command was even sent, which is a major contributor
     * to perceived remote-control latency. Caching it reactively makes each read a free in-memory
     * StateFlow access instead.
     */
    private val disabledVibrationStateFlow = settings.data
        .map { it.disabled_vibration }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        lifecycleOwner.lifecycle.subscribe(streamingRepository)
    }

    fun getFlipperScreen(): StateFlow<FlipperScreenState> = streamingRepository.getFlipperScreen()
    fun getFlipperButtons() = buttonStackRepository.getButtonStack()
    fun getLockState() = lockRepository.getLockState()
    fun onChangeLock(isWillBeLocked: Boolean) = lockRepository.onChangeLock(isWillBeLocked)
    fun onPressButton(
        buttonEnum: ButtonEnum,
        inputType: InputType
    ) {
        vibrator?.vibrateCompat(VIBRATOR_TIME_MS, disabledVibrationStateFlow.value)

        val uuid = buttonStackRepository.onNewStackButton(buttonEnum.animEnum)
        flipperButtonRepository.pressOnButton(
            viewModelScope = viewModelScope,
            key = buttonEnum.key,
            type = inputType,
            onComplete = {
                buttonStackRepository.onRemoveStackButton(uuid)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.lifecycle.unsubscribe(streamingRepository)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            lifecycleOwner: LifecycleOwner
        ): ScreenStreamingViewModel
    }
}
