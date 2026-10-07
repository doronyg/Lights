package com.doronyg.mylightscontroller.onboarding

import com.doronyg.mylightscontroller.onboarding.qr.QrCode
import com.doronyg.mylightscontroller.onboarding.qr.QrEncoder
import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import kotlinx.coroutines.withContext

/**
 * QR code data generator - pure multiplatform logic.
 *
 * Encodes content as QR code and returns pixel data as IntArray.
 * Platform-specific rendering (Bitmap, Canvas, etc.) is handled by the caller.
 */
object QrImageGenerator {

    // ARGB color constants
    private const val BLACK = 0xFF000000.toInt()  // ARGB: fully opaque black
    private const val WHITE = 0xFFFFFFFF.toInt()  // ARGB: fully opaque white

    /**
     * Generate QR code pixel data.
     *
     * @param content The content to encode in the QR code
     * @param size The size of the generated QR code in pixels
     * @return Pair of (width, height, pixels) where pixels is an IntArray of ARGB colors
     */
    suspend fun generateQrPixels(
        content: String,
        size: Int = 800
    ): QrPixelData = withContext(PlatformBridge.dispatchers.defaultDispatcher()) {
        render(QrEncoder.encodeText(content), size)
    }

    /** Quiet-zone width in modules, as required by the QR spec. */
    private const val QUIET_ZONE = 4

    /**
     * Rasterize [qr] into a square image of at least [size] pixels, with a 4-module
     * quiet zone, integer module scaling and centered padding (same layout ZXing used).
     */
    internal fun render(qr: QrCode, size: Int): QrPixelData {
        val fullModules = qr.size + QUIET_ZONE * 2
        val outSize = maxOf(size, fullModules)
        val scale = outSize / fullModules
        val offset = (outSize - qr.size * scale) / 2

        val pixels = IntArray(outSize * outSize) { WHITE }
        for (my in 0 until qr.size) {
            for (mx in 0 until qr.size) {
                if (!qr.isDark(mx, my)) continue
                val startX = offset + mx * scale
                val startY = offset + my * scale
                for (py in startY until startY + scale) {
                    pixels.fill(BLACK, py * outSize + startX, py * outSize + startX + scale)
                }
            }
        }
        return QrPixelData(outSize, outSize, pixels)
    }
}

/**
 * Container for QR code pixel data.
 * Platform-agnostic representation of QR code image data.
 */
data class QrPixelData(
    val width: Int,
    val height: Int,
    val pixels: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QrPixelData) return false

        if (width != other.width) return false
        if (height != other.height) return false
        if (!pixels.contentEquals(other.pixels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}

