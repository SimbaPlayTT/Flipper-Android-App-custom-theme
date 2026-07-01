package com.flipperdevices.settings.impl.composable.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import com.flipperdevices.core.ui.ktx.elements.ComposableFlipperButton
import com.flipperdevices.core.ui.theme.LocalPallet
import com.flipperdevices.core.ui.theme.LocalTypography
import com.flipperdevices.settings.impl.R
import com.flipperdevices.settings.impl.viewmodels.FeedbackSendState
import com.flipperdevices.settings.impl.viewmodels.FeedbackViewModel

@Composable
fun FeedbackDialog(
    viewModel: FeedbackViewModel,
    onDismiss: () -> Unit
) {
    val sendState by viewModel.getSendState().collectAsState()
    var message by remember { mutableStateOf("") }

    LaunchedEffect(sendState) {
        if (sendState is FeedbackSendState.Sent) {
            onDismiss()
            viewModel.reset()
        }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.feedback_title),
                    style = LocalTypography.current.titleB18,
                    color = LocalPallet.current.text100
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalPallet.current.hexKeyboardBackground)
                        .padding(12.dp)
                ) {
                    if (message.isEmpty()) {
                        Text(
                            text = stringResource(R.string.feedback_hint),
                            style = LocalTypography.current.bodyR16,
                            color = LocalPallet.current.text30
                        )
                    }
                    BasicTextField(
                        modifier = Modifier.fillMaxSize(),
                        value = message,
                        onValueChange = { message = it },
                        textStyle = LocalTypography.current.bodyR16.copy(color = LocalPallet.current.text100),
                        cursorBrush = SolidColor(LocalPallet.current.text100)
                    )
                }

                if (sendState is FeedbackSendState.Error) {
                    Text(
                        text = stringResource(R.string.feedback_error),
                        style = LocalTypography.current.subtitleB12,
                        color = LocalPallet.current.onError
                    )
                }

                ComposableFlipperButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.feedback_send),
                    isLoading = sendState is FeedbackSendState.Sending,
                    enabled = message.isNotBlank(),
                    onClick = { viewModel.sendFeedback(message) }
                )
            }
        }
    }
}
