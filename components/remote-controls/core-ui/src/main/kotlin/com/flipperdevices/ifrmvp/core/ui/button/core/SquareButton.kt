package com.flipperdevices.ifrmvp.core.ui.button.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.flipperdevices.core.ui.ktx.onScrollHoldPress
import com.flipperdevices.core.ui.theme.LocalPallet
import com.flipperdevices.ifrmvp.core.ui.layout.core.sf
import com.flipperdevices.ifrmvp.core.ui.util.GridConstants

// todo remove after design colors changed
val buttonBackgroundColor: Color
    @Composable
    get() = LocalPallet.current.dPadAccent

val buttonBackgroundVariantColor: Color
    @Composable
    get() = LocalPallet.current.dPadAccent.copy(alpha = 0.7f)

@Composable
fun SquareButton(
    onClick: ((ButtonClickEvent) -> Unit)?,
    background: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(GridConstants.DEFAULT_BUTTON_SIZE.sf)
            .clip(RoundedCornerShape(8.sf))
            .background(background)
            .onScrollHoldPress { onClick?.invoke(it) },
        contentAlignment = Alignment.Center,
        content = {
            content.invoke(this)
        }
    )
}
