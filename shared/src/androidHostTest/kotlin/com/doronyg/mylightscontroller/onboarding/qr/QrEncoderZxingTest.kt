package com.doronyg.mylightscontroller.onboarding.qr

import com.doronyg.mylightscontroller.onboarding.QrImageGenerator
import com.doronyg.mylightscontroller.onboarding.qr.QrEncoderTest.Companion.randomAscii
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.Decoder
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies our encoder against ZXing's decoder (JVM-only, test-only dependency).
 * Stays in the Android/JVM test source set after the KMP split.
 */
class QrEncoderZxingTest {

    private fun decodeModules(qr: QrCode): String {
        val m = BitMatrix(qr.size)
        for (y in 0 until qr.size) for (x in 0 until qr.size) if (qr.isDark(x, y)) m.set(x, y)
        return Decoder().decode(m).text
    }

    @Test
    fun roundTripAcrossAllVersionsAndEccLevels() {
        for (ecc in QrEncoder.Ecc.entries) {
            var len = 1
            while (true) {
                val text = randomAscii(len, seed = len * 31 + ecc.ordinal)
                val qr = try {
                    QrEncoder.encodeText(text, ecc, boostEcc = false)
                } catch (_: IllegalArgumentException) {
                    break
                }
                assertEquals(text, decodeModules(qr), "v${qr.version} $ecc len=$len")
                len += 7
            }
        }
    }

    @Test
    fun utf8RoundTrip() {
        val text = "mylightscontroller://setup?name=סלון&emoji=💡"
        assertEquals(text, decodeModules(QrEncoder.encodeText(text)))
    }

    @Test
    fun renderedImageDecodesWithQuietZone() {
        val text = "mylightscontroller://setup?" + randomAscii(300, 7)
        val img = QrImageGenerator.render(QrEncoder.encodeText(text), 800)

        val source = RGBLuminanceSource(img.width, img.height, img.pixels)
        val result = QRCodeReader().decode(
            BinaryBitmap(HybridBinarizer(source)),
            mapOf(DecodeHintType.PURE_BARCODE to true)
        )
        assertEquals(text, result.text)
    }
}

