package com.doronyg.mylightscontroller.onboarding

import com.doronyg.mylightscontroller.platform.common.PlatformBridge
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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
        val bitMatrix = QRCodeWriter()
            .encode(content, BarcodeFormat.QR_CODE, size, size)

        val width = bitMatrix.width
        val height = bitMatrix.height

        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                pixels[row + x] = if (bitMatrix[x, y]) BLACK else WHITE
            }
        }

        QrPixelData(width, height, pixels)
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
        if (javaClass != other?.javaClass) return false

        other as QrPixelData

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

