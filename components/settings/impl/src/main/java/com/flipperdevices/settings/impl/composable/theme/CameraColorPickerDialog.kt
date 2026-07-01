package com.flipperdevices.settings.impl.composable.theme

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.flipperdevices.core.ui.ktx.clickableRipple
import com.flipperdevices.core.ui.ktx.elements.ComposableFlipperButton
import com.flipperdevices.core.ui.theme.composable.color.toHexString
import com.flipperdevices.settings.impl.R

private const val CENTER_SAMPLE_DOWNSAMPLE = 4

/**
 * Lets the user point the camera at their physical Flipper shell and capture its exact color
 * instead of eyeballing sliders - samples a single pixel at the center of the captured frame
 * (marked with the on-screen reticle) rather than running continuous frame analysis, which keeps
 * this simple and avoids YUV plane math entirely (ImageCapture hands back a plain JPEG we can
 * decode with BitmapFactory).
 */
@Composable
fun CameraColorPickerDialog(
    onColorPicked: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) onDismiss()
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasPermission) {
                CameraContent(onColorPicked = onColorPicked, onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun CameraContent(
    onColorPicked: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    var sampledColor by remember { mutableStateOf<Color?>(null) }
    var captureFailed by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = CameraXPreview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        } catch (bindException: IllegalStateException) {
                            captureFailed = true
                        } catch (bindException: IllegalArgumentException) {
                            captureFailed = true
                        }
                    },
                    ContextCompat.getMainExecutor(ctx)
                )
                previewView
            }
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .border(width = 2.dp, color = Color.White, shape = CircleShape)
        )

        Text(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .clickableRipple(onClick = onDismiss)
                .padding(8.dp),
            text = "✕",
            color = Color.White
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (captureFailed) {
                Text(
                    text = stringResource(R.string.theme_picker_camera_error),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            val currentColor = sampledColor
            if (currentColor != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                            .border(width = 1.dp, color = Color.White, shape = CircleShape)
                    )
                    Text(text = "#" + currentColor.toHexString(), color = Color.White)
                }
                ComposableFlipperButton(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(R.string.theme_picker_done),
                    onClick = {
                        onColorPicked(currentColor)
                        onDismiss()
                    }
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(width = 3.dp, color = Color.Gray, shape = CircleShape)
                    .clickableRipple {
                        imageCapture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    try {
                                        sampledColor = sampleCenterColor(image)
                                        captureFailed = sampledColor == null
                                    } finally {
                                        image.close()
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    captureFailed = true
                                }
                            }
                        )
                    }
            )
        }
    }
}

private fun sampleCenterColor(image: ImageProxy): Color? {
    return try {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val options = BitmapFactory.Options().apply { inSampleSize = CENTER_SAMPLE_DOWNSAMPLE }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
        Color(bitmap.getPixel(bitmap.width / 2, bitmap.height / 2))
    } catch (decodeException: IllegalArgumentException) {
        null
    }
}
