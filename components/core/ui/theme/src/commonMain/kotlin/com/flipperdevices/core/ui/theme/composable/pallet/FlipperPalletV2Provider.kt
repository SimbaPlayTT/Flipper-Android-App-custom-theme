package com.flipperdevices.core.ui.theme.composable.pallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.flipperdevices.core.ui.theme.composable.pallet.generated.FlipperPalletV2
import com.flipperdevices.core.ui.theme.composable.pallet.generated.getDarkPallet
import com.flipperdevices.core.ui.theme.composable.pallet.generated.getLightPallet
import com.flipperdevices.core.ui.theme.composable.pallet.generated.toAnimatePallet

/**
 * Please, use instead LocalPalletV2
 *
 * @return the necessary Pallet depending on the theme
 */
@Composable
fun getThemedFlipperPalletV2(isLight: Boolean, customAccent: Color? = null): FlipperPalletV2 {
    val base = remember(isLight) {
        if (isLight) {
            getLightPallet()
        } else {
            getDarkPallet()
        }
    }
    val overridden = if (customAccent != null) {
        base.copy(
            surface = base.surface.copy(
                navBar = base.surface.navBar.copy(
                    body = base.surface.navBar.body.copy(accentBrand = customAccent)
                ),
                border = base.surface.border.copy(
                    accentBrand = base.surface.border.accentBrand.copy(
                        primary = customAccent,
                        secondary = customAccent,
                        tertiary = customAccent
                    )
                )
            ),
            action = base.action.copy(
                brand = base.action.brand.copy(
                    background = base.action.brand.background.copy(
                        primary = base.action.brand.background.primary.copy(default = customAccent),
                        secondary = base.action.brand.background.secondary.copy(default = customAccent),
                        tertiary = base.action.brand.background.tertiary.copy(default = customAccent)
                    ),
                    text = base.action.brand.text.copy(default = customAccent),
                    border = base.action.brand.border.copy(
                        primary = base.action.brand.border.primary.copy(default = customAccent),
                        secondary = base.action.brand.border.secondary.copy(default = customAccent),
                        tertiary = base.action.brand.border.tertiary.copy(default = customAccent)
                    ),
                    icon = base.action.brand.icon.copy(default = customAccent)
                )
            ),
            illustration = base.illustration.copy(
                brand = base.illustration.brand.copy(
                    primary = customAccent,
                    secondary = customAccent,
                    tertiary = customAccent
                )
            ),
            icon = base.icon.copy(
                brand = base.icon.brand.copy(
                    primary = customAccent,
                    secondary = customAccent,
                    tertiary = customAccent
                )
            )
        )
    } else {
        base
    }
    return overridden.toAnimatePallet()
}
