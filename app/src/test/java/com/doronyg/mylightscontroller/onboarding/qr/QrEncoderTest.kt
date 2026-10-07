package com.doronyg.mylightscontroller.onboarding.qr

import com.doronyg.mylightscontroller.onboarding.QrImageGenerator
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.Decoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Verifies our encoder against ZXing's decoder (test-only dependency).
 */
class QrEncoderTest {

    private fun decodeModules(qr: QrCode): String {
        val m = BitMatrix(qr.size)
        for (y in 0 until qr.size) for (x in 0 until qr.size) if (qr.isDark(x, y)) m.set(x, y)
        return Decoder().decode(m).text
    }

    private fun randomAscii(len: Int, seed: Int): String {
        val r = Random(seed)
        val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~:/?#[]@!$&'()*+,;=%"
        return buildString { repeat(len) { append(alphabet[r.nextInt(alphabet.length)]) } }
    }

    @Test
    fun roundTripAcrossAllVersionsAndEccLevels() {
        val seenVersions = mutableSetOf<Int>()
        for (ecc in QrEncoder.Ecc.entries) {
            var len = 1
            while (true) {
                val text = randomAscii(len, seed = len * 31 + ecc.ordinal)
                val qr = try {
                    QrEncoder.encodeText(text, ecc, boostEcc = false)
                } catch (_: IllegalArgumentException) {
                    break
                }
                assertEquals(qr.version * 4 + 17, qr.size)
                assertEquals("v${qr.version} $ecc len=$len", text, decodeModules(qr))
                seenVersions += qr.version
                len += 7
            }
        }
        assertEquals((1..40).toSet(), seenVersions)
    }

    @Test
    fun allMaskPatternsGetExercised() {
        val masks = (1..400).map { QrEncoder.encodeText(randomAscii(it, it)).mask }.toSet()
        assertEquals((0..7).toSet(), masks)
    }

    @Test
    fun utf8RoundTrip() {
        val text = "mylightscontroller://setup?name=סלון&emoji=💡"
        assertEquals(text, decodeModules(QrEncoder.encodeText(text)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun tooLongThrows() {
        QrEncoder.encodeText("x".repeat(3000), QrEncoder.Ecc.LOW)
    }

    @Test
    fun renderedImageDecodesWithQuietZone() {
        val text = "mylightscontroller://setup?" + randomAscii(300, 7)
        val qr = QrEncoder.encodeText(text)
        val img = QrImageGenerator.render(qr, 800)

        assertEquals(800, img.width)
        assertEquals(800, img.height)
        // Corners must be white (quiet zone).
        assertEquals(-1, img.pixels[0])
        assertEquals(-1, img.pixels[img.pixels.size - 1])

        val source = RGBLuminanceSource(img.width, img.height, img.pixels)
        val result = QRCodeReader().decode(
            BinaryBitmap(HybridBinarizer(source)),
            mapOf(DecodeHintType.PURE_BARCODE to true)
        )
        assertEquals(text, result.text)
    }

    @Test
    fun renderNeverSmallerThanSymbol() {
        val qr = QrEncoder.encodeText("hi")
        val img = QrImageGenerator.render(qr, 1)
        assertTrue(img.width >= qr.size + 8)
    }
}

