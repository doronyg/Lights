package com.doronyg.mylightscontroller.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.doronyg.mylightscontroller.onboarding.qr.QrCode
import kotlin.math.floor

/** Quiet-zone width in modules, as required by the QR spec. */
private const val QUIET_ZONE = 4

/**
 * Draws a [QrCode] with plain Compose drawing (no platform bitmaps), including the
 * white quiet zone. Modules are snapped to whole pixels so scanners see crisp edges.
 */
@Composable
fun QrCodeImage(
    qr: QrCode,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else modifier

    Canvas(semanticsModifier) {
        val totalModules = qr.size + QUIET_ZONE * 2
        val side = size.minDimension

        // Whole-pixel module size avoids anti-aliased seams between modules
        val cell = floor(side / totalModules).takeIf { it >= 1f } ?: (side / totalModules)
        val drawnSide = cell * totalModules
        val origin = Offset(
            (size.width - drawnSide) / 2f + QUIET_ZONE * cell,
            (size.height - drawnSide) / 2f + QUIET_ZONE * cell
        )

        drawRect(Color.White)

        for (y in 0 until qr.size) {
            // Merge horizontal runs of dark modules into single rects
            var x = 0
            while (x < qr.size) {
                if (!qr.isDark(x, y)) {
                    x++
                    continue
                }
                val start = x
                while (x < qr.size && qr.isDark(x, y)) x++
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(origin.x + start * cell, origin.y + y * cell),
                    size = Size((x - start) * cell, cell)
                )
            }
        }
    }
}

