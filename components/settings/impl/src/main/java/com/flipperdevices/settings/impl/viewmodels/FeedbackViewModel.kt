package com.flipperdevices.settings.impl.viewmodels

import com.flipperdevices.core.ui.lifecycle.DecomposeViewModel
import com.flipperdevices.settings.impl.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DISCORD_CONTENT_MAX_LENGTH = 1800
private const val CONTROL_CHAR_BOUNDARY = 0x20

sealed interface FeedbackSendState {
    data object Idle : FeedbackSendState
    data object Sending : FeedbackSendState
    data object Sent : FeedbackSendState
    data object Error : FeedbackSendState
}

class FeedbackViewModel @Inject constructor(
    private val httpClient: HttpClient
) : DecomposeViewModel() {
    private val sendStateFlow = MutableStateFlow<FeedbackSendState>(FeedbackSendState.Idle)

    fun getSendState(): StateFlow<FeedbackSendState> = sendStateFlow.asStateFlow()

    fun reset() {
        sendStateFlow.value = FeedbackSendState.Idle
    }

    fun sendFeedback(message: String) {
        val trimmed = message.trim()
        val webhookUrl = BuildConfig.FEEDBACK_WEBHOOK_URL
        if (trimmed.isEmpty() || webhookUrl.isEmpty()) {
            sendStateFlow.value = FeedbackSendState.Error
            return
        }
        sendStateFlow.value = FeedbackSendState.Sending
        viewModelScope.launch {
            try {
                httpClient.post(webhookUrl) {
                    contentType(ContentType.Application.Json)
                    setBody("{\"content\":\"${escapeJson(trimmed)}\"}")
                }
                sendStateFlow.value = FeedbackSendState.Sent
            } catch (sendException: Exception) {
                sendStateFlow.value = FeedbackSendState.Error
            }
        }
    }
}

private fun escapeJson(text: String): String {
    val truncated = text.take(DISCORD_CONTENT_MAX_LENGTH)
    return buildString {
        truncated.forEach { char ->
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (char.code >= CONTROL_CHAR_BOUNDARY) append(char)
            }
        }
    }
}
