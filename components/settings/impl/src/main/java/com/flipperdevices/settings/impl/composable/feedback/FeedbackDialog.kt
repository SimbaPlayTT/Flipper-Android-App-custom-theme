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

private const val FEEDBACK_ISSUES_URL =
    "https://github.com/SimbaPlayTT/Flipper-Android-App-custom-theme/issues/new"
private const val FEEDBACK_ISSUE_TITLE = "App Feedback"

/**
 * Opens a pre-filled "New Issue" page on the public repo instead of posting to a webhook or a
 * personal inbox - a webhook secret baked into the app would be recoverable by decompiling the
 * APK (this app is distributed as a public build), and routing to a personal email isn't
 * appropriate for a public feedback channel either. A GitHub issue needs no embedded secret,
 * works identically for every install, and is visible to the maintainer through normal GitHub
 * notifications.
 */
@Composable
fun FeedbackDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var noBrowserApp by remember { mutableStateOf(false) }

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
                            noBrowserApp = false
                        },
                        textStyle = LocalTypography.current.bodyR16.copy(color = LocalPallet.current.text100),
                        cursorBrush = SolidColor(LocalPallet.current.text100)
                    )
                }

                if (noBrowserApp) {
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
                        val issueUrl = Uri.parse(FEEDBACK_ISSUES_URL).buildUpon()
                            .appendQueryParameter("title", FEEDBACK_ISSUE_TITLE)
                            .appendQueryParameter("body", message)
                            .build()
                        val browserIntent = Intent(Intent.ACTION_VIEW, issueUrl)
                        try {
                            context.startActivity(browserIntent)
                            onDismiss()
                        } catch (activityNotFoundException: ActivityNotFoundException) {
                            noBrowserApp = true
                        }
                    }
                )
            }
        }
    }
}
