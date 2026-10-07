package com.doronyg.mylightscontroller.onboarding.qr

import com.doronyg.mylightscontroller.onboarding.QrImageGenerator
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Platform-independent encoder tests (kotlin.test only).
 * Ready to move to `commonTest` once the shared KMP module exists.
 * Decoding round-trips against ZXing live in [QrEncoderZxingTest] (JVM-only).
 */
class QrEncoderTest {

    @Test
    fun sizeMatchesVersionAcrossAllVersionsAndEccLevels() {
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
    fun tooLongThrows() {
        assertFailsWith<IllegalArgumentException> {
            QrEncoder.encodeText("x".repeat(3000), QrEncoder.Ecc.LOW)
        }
    }

    @Test
    fun renderedImageHasRequestedSizeAndQuietZone() {
        val qr = QrEncoder.encodeText("mylightscontroller://setup?" + randomAscii(300, 7))
        val img = QrImageGenerator.render(qr, 800)

        assertEquals(800, img.width)
        assertEquals(800, img.height)
        // Corners must be white (quiet zone).
        assertEquals(-1, img.pixels[0])
        assertEquals(-1, img.pixels[img.pixels.size - 1])
    }

    @Test
    fun renderNeverSmallerThanSymbol() {
        val qr = QrEncoder.encodeText("hi")
        val img = QrImageGenerator.render(qr, 1)
        assertTrue(img.width >= qr.size + 8)
    }

    companion object {
        fun randomAscii(len: Int, seed: Int): String {
            val r = Random(seed)
            val alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~:/?#[]@!$&'()*+,;=%"
            return buildString { repeat(len) { append(alphabet[r.nextInt(alphabet.length)]) } }
        }
    }
}

