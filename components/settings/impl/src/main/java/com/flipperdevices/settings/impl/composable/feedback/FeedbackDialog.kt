package com.flipperdevices.settings.impl.composable.feedback

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flipperdevices.core.ui.ktx.clickableRipple
import com.flipperdevices.core.ui.ktx.elements.ComposableFlipperButton
import com.flipperdevices.core.ui.theme.LocalPallet
import com.flipperdevices.core.ui.theme.LocalTypography
import com.flipperdevices.settings.impl.R

private const val FEEDBACK_EMAIL = "serajwazzaz2010@gmail.com"

/**
 * Sends feedback via a mailto intent instead of posting to a webhook - a webhook secret baked
 * into the app would be recoverable by decompiling the APK (and this app is distributed as a
 * public build), so anyone who downloaded it could abuse it. Email needs no embedded secret and
 * works the same for every install.
 */
@Composable
fun FeedbackDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var noEmailApp by remember { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.feedback_title),
                        style = LocalTypography.current.titleB18,
                        color = LocalPallet.current.text100
                    )
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickableRipple(onClick = onDismiss)
                            .padding(8.dp),
                        text = "✕",
                        style = LocalTypography.current.titleB18,
                        color = LocalPallet.current.text60
                    )
                }

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
                        onValueChange = {
                            message = it
                            noEmailApp = false
                        },
                        textStyle = LocalTypography.current.bodyR16.copy(color = LocalPallet.current.text100),
                        cursorBrush = SolidColor(LocalPallet.current.text100)
                    )
                }

                if (noEmailApp) {
                    Text(
                        text = stringResource(R.string.feedback_error),
                        style = LocalTypography.current.subtitleB12,
                        color = LocalPallet.current.onError
                    )
                }

                ComposableFlipperButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.feedback_send),
                    enabled = message.isNotBlank(),
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
                            putExtra(Intent.EXTRA_SUBJECT, "Flipper App Feedback")
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        try {
                            context.startActivity(emailIntent)
                            onDismiss()
                        } catch (activityNotFoundException: ActivityNotFoundException) {
                            noEmailApp = true
                        }
                    }
                )
            }
        }
    }
}
