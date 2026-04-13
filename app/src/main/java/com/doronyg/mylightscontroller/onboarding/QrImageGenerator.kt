package com.doronyg.mylightscontroller.onboarding

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrImageGenerator {

    suspend fun generateQrBitmap(
        content: String,
        size: Int = 800
    ): Bitmap = withContext(Dispatchers.Default) {

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

        createBitmap(width, height).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}