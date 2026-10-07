package com.doronyg.mylightscontroller.onboarding.qr

import kotlin.math.abs

/**
 * Pure Kotlin QR Code encoder (Model 2, versions 1–40, byte mode).
 *
 * Implements ISO/IEC 18004. Structure follows the well-known public design
 * by Project Nayuki (https://www.nayuki.io/page/qr-code-generator-library, MIT).
 * No platform or third-party dependencies, so it can move to `commonMain` unchanged.
 */
object QrEncoder {

    enum class Ecc(internal val ordinalIdx: Int, internal val formatBits: Int) {
        LOW(0, 1),      // ~7% recovery
        MEDIUM(1, 0),   // ~15%
        QUARTILE(2, 3), // ~25%
        HIGH(3, 2),     // ~30%
    }

    const val MIN_VERSION = 1
    const val MAX_VERSION = 40

    /** Encode UTF-8 text. */
    fun encodeText(text: String, ecc: Ecc = Ecc.MEDIUM, boostEcc: Boolean = true): QrCode =
        encodeBytes(text.encodeToByteArray(), ecc, boostEcc)

    /**
     * Encode raw bytes using byte mode. Picks the smallest version that fits and,
     * if [boostEcc] is true, the highest ECC level that still fits that version.
     *
     * @throws IllegalArgumentException if the data is too long for any version.
     */
    fun encodeBytes(data: ByteArray, ecc: Ecc = Ecc.MEDIUM, boostEcc: Boolean = true): QrCode {
        var version = MIN_VERSION
        var usedBits: Int
        while (true) {
            usedBits = 4 + charCountBits(version) + data.size * 8
            if (data.size < (1 shl charCountBits(version)) &&
                usedBits <= numDataCodewords(version, ecc) * 8
            ) break
            require(version < MAX_VERSION) { "Data too long for a QR code: ${data.size} bytes" }
            version++
        }

        var finalEcc = ecc
        if (boostEcc) {
            for (e in listOf(Ecc.MEDIUM, Ecc.QUARTILE, Ecc.HIGH)) {
                if (e.ordinalIdx > finalEcc.ordinalIdx && usedBits <= numDataCodewords(version, e) * 8) {
                    finalEcc = e
                }
            }
        }

        // ---- Build the data bit stream ----
        val bb = BitBuffer()
        bb.append(0x4, 4) // byte mode indicator
        bb.append(data.size, charCountBits(version))
        for (b in data) bb.append(b.toInt() and 0xFF, 8)

        val capacityBits = numDataCodewords(version, finalEcc) * 8
        bb.append(0, minOf(4, capacityBits - bb.size)) // terminator
        bb.append(0, (8 - bb.size % 8) % 8)             // byte align
        var pad = 0xEC
        while (bb.size < capacityBits) {
            bb.append(pad, 8)
            pad = pad xor (0xEC xor 0x11)
        }

        return QrCode.build(version, finalEcc, bb.toBytes())
    }

    // ---------------- Tables & capacity helpers ----------------

    private fun charCountBits(version: Int): Int = if (version <= 9) 8 else 16

    internal fun numRawDataModules(ver: Int): Int {
        var result = (16 * ver + 128) * ver + 64
        if (ver >= 2) {
            val numAlign = ver / 7 + 2
            result -= (25 * numAlign - 10) * numAlign - 55
            if (ver >= 7) result -= 36
        }
        return result
    }

    internal fun numDataCodewords(ver: Int, ecc: Ecc): Int =
        numRawDataModules(ver) / 8 -
            ECC_CODEWORDS_PER_BLOCK[ecc.ordinalIdx][ver] * NUM_ERROR_CORRECTION_BLOCKS[ecc.ordinalIdx][ver]

    internal val ECC_CODEWORDS_PER_BLOCK: Array<IntArray> = arrayOf(
        // Index 0 is padding; versions 1..40
        intArrayOf(-1, 7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 28, 30, 30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30), // L
        intArrayOf(-1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26, 26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28), // M
        intArrayOf(-1, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 30, 28, 30, 30, 30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30), // Q
        intArrayOf(-1, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 24, 28, 22, 24, 24, 30, 28, 28, 26, 28, 30, 24, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30), // H
    )

    internal val NUM_ERROR_CORRECTION_BLOCKS: Array<IntArray> = arrayOf(
        intArrayOf(-1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 4, 6, 6, 6, 6, 7, 8, 8, 9, 9, 10, 12, 12, 12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25), // L
        intArrayOf(-1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49), // M
        intArrayOf(-1, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25, 27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68), // Q
        intArrayOf(-1, 1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30, 32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81), // H
    )

    private class BitBuffer {
        private val bits = ArrayList<Boolean>()
        val size: Int get() = bits.size

        fun append(value: Int, length: Int) {
            for (i in length - 1 downTo 0) bits.add(((value ushr i) and 1) != 0)
        }

        fun toBytes(): IntArray {
            val out = IntArray(bits.size / 8)
            for (i in bits.indices) if (bits[i]) out[i ushr 3] = out[i ushr 3] or (0x80 ushr (i and 7))
            return out
        }
    }
}

/**
 * Immutable QR symbol: a square grid of dark/light modules (no quiet zone).
 */
class QrCode private constructor(
    val version: Int,
    val ecc: QrEncoder.Ecc,
    val mask: Int,
    private val modules: Array<BooleanArray>,
) {
    val size: Int get() = modules.size

    /** True if the module at column [x], row [y] is dark. Out-of-range coordinates are light. */
    fun isDark(x: Int, y: Int): Boolean =
        x in 0 until size && y in 0 until size && modules[y][x]

    internal companion object {

        fun build(version: Int, ecc: QrEncoder.Ecc, dataCodewords: IntArray): QrCode {
            val b = Builder(version, ecc)
            b.drawFunctionPatterns()
            b.drawCodewords(b.addEccAndInterleave(dataCodewords))

            var bestMask = 0
            var bestPenalty = Int.MAX_VALUE
            for (m in 0 until 8) {
                b.applyMask(m)
                b.drawFormatBits(m)
                val p = b.penaltyScore()
                if (p < bestPenalty) {
                    bestPenalty = p
                    bestMask = m
                }
                b.applyMask(m) // XOR again to undo
            }
            b.applyMask(bestMask)
            b.drawFormatBits(bestMask)
            return QrCode(version, ecc, bestMask, b.modules)
        }
    }

    private class Builder(val version: Int, val ecc: QrEncoder.Ecc) {
        val size = version * 4 + 17
        val modules = Array(size) { BooleanArray(size) }
        val isFunction = Array(size) { BooleanArray(size) }

        private fun setFunction(x: Int, y: Int, dark: Boolean) {
            modules[y][x] = dark
            isFunction[y][x] = true
        }

        // ---------- Function patterns ----------

        fun drawFunctionPatterns() {
            for (i in 0 until size) { // timing patterns
                setFunction(6, i, i % 2 == 0)
                setFunction(i, 6, i % 2 == 0)
            }
            drawFinder(3, 3)
            drawFinder(size - 4, 3)
            drawFinder(3, size - 4)

            val align = alignmentPositions()
            val n = align.size
            for (i in 0 until n) for (j in 0 until n) {
                val overlapsFinder = (i == 0 && j == 0) || (i == 0 && j == n - 1) || (i == n - 1 && j == 0)
                if (!overlapsFinder) drawAlignment(align[i], align[j])
            }

            drawFormatBits(0) // reserve area; real value drawn after mask selection
            drawVersion()
        }

        private fun drawFinder(cx: Int, cy: Int) {
            for (dy in -4..4) for (dx in -4..4) {
                val x = cx + dx
                val y = cy + dy
                if (x in 0 until size && y in 0 until size) {
                    val dist = maxOf(abs(dx), abs(dy))
                    setFunction(x, y, dist != 2 && dist != 4)
                }
            }
        }

        private fun drawAlignment(cx: Int, cy: Int) {
            for (dy in -2..2) for (dx in -2..2) {
                setFunction(cx + dx, cy + dy, maxOf(abs(dx), abs(dy)) != 1)
            }
        }

        private fun alignmentPositions(): IntArray {
            if (version == 1) return IntArray(0)
            val numAlign = version / 7 + 2
            val step = (version * 8 + numAlign * 3 + 5) / (numAlign * 4 - 4) * 2
            val result = IntArray(numAlign)
            result[0] = 6
            var pos = size - 7
            for (i in numAlign - 1 downTo 1) {
                result[i] = pos
                pos -= step
            }
            return result
        }

        fun drawFormatBits(mask: Int) {
            val data = (ecc.formatBits shl 3) or mask
            var rem = data
            repeat(10) { rem = (rem shl 1) xor ((rem ushr 9) * 0x537) }
            val bits = ((data shl 10) or rem) xor 0x5412

            // First copy (around top-left finder)
            for (i in 0..5) setFunction(8, i, bit(bits, i))
            setFunction(8, 7, bit(bits, 6))
            setFunction(8, 8, bit(bits, 7))
            setFunction(7, 8, bit(bits, 8))
            for (i in 9 until 15) setFunction(14 - i, 8, bit(bits, i))

            // Second copy (split between top-right and bottom-left finders)
            for (i in 0 until 8) setFunction(size - 1 - i, 8, bit(bits, i))
            for (i in 8 until 15) setFunction(8, size - 15 + i, bit(bits, i))
            setFunction(8, size - 8, true) // always-dark module
        }

        private fun drawVersion() {
            if (version < 7) return
            var rem = version
            repeat(12) { rem = (rem shl 1) xor ((rem ushr 11) * 0x1F25) }
            val bits = (version shl 12) or rem
            for (i in 0 until 18) {
                val dark = bit(bits, i)
                val a = size - 11 + i % 3
                val b = i / 3
                setFunction(a, b, dark)
                setFunction(b, a, dark)
            }
        }

        // ---------- Error correction ----------

        fun addEccAndInterleave(data: IntArray): IntArray {
            val e = ecc.ordinalIdx
            val numBlocks = QrEncoder.NUM_ERROR_CORRECTION_BLOCKS[e][version]
            val blockEccLen = QrEncoder.ECC_CODEWORDS_PER_BLOCK[e][version]
            val rawCodewords = QrEncoder.numRawDataModules(version) / 8
            val numShortBlocks = numBlocks - rawCodewords % numBlocks
            val shortBlockLen = rawCodewords / numBlocks

            val divisor = ReedSolomon.computeDivisor(blockEccLen)
            val blocks = ArrayList<IntArray>(numBlocks)
            var k = 0
            for (i in 0 until numBlocks) {
                val datLen = shortBlockLen - blockEccLen + if (i < numShortBlocks) 0 else 1
                val dat = data.copyOfRange(k, k + datLen)
                k += datLen
                val eccBytes = ReedSolomon.computeRemainder(dat, divisor)
                // Short blocks get a placeholder so all blocks align; it's skipped when interleaving.
                val block = IntArray(shortBlockLen + 1)
                dat.copyInto(block, 0)
                eccBytes.copyInto(block, shortBlockLen + 1 - blockEccLen)
                blocks.add(block)
            }

            val result = IntArray(rawCodewords)
            var idx = 0
            for (i in 0..shortBlockLen) {
                for (j in 0 until numBlocks) {
                    if (i != shortBlockLen - blockEccLen || j >= numShortBlocks) {
                        result[idx++] = blocks[j][i]
                    }
                }
            }
            return result
        }

        // ---------- Data placement ----------

        fun drawCodewords(data: IntArray) {
            var i = 0
            var right = size - 1
            while (right >= 1) {
                if (right == 6) right = 5 // skip vertical timing column
                val upward = ((right + 1) and 2) == 0
                for (vert in 0 until size) {
                    val y = if (upward) size - 1 - vert else vert
                    for (j in 0..1) {
                        val x = right - j
                        if (!isFunction[y][x] && i < data.size * 8) {
                            modules[y][x] = bit(data[i ushr 3], 7 - (i and 7))
                            i++
                        }
                        // Remaining remainder bits stay light (0).
                    }
                }
                right -= 2
            }
        }

        fun applyMask(mask: Int) {
            for (y in 0 until size) for (x in 0 until size) {
                val invert = when (mask) {
                    0 -> (x + y) % 2 == 0
                    1 -> y % 2 == 0
                    2 -> x % 3 == 0
                    3 -> (x + y) % 3 == 0
                    4 -> (x / 3 + y / 2) % 2 == 0
                    5 -> x * y % 2 + x * y % 3 == 0
                    6 -> (x * y % 2 + x * y % 3) % 2 == 0
                    7 -> ((x + y) % 2 + x * y % 3) % 2 == 0
                    else -> error("Invalid mask $mask")
                }
                if (invert && !isFunction[y][x]) modules[y][x] = !modules[y][x]
            }
        }

        // ---------- Mask penalty (ISO/IEC 18004 §7.8.3) ----------

        fun penaltyScore(): Int {
            var result = 0

            // Rule 1: runs of 5+ same-colored modules in rows and columns.
            for (y in 0 until size) result += runPenalty { modules[y][it] }
            for (x in 0 until size) result += runPenalty { modules[it][x] }

            // Rule 2: 2x2 blocks of the same color.
            for (y in 0 until size - 1) for (x in 0 until size - 1) {
                val c = modules[y][x]
                if (c == modules[y][x + 1] && c == modules[y + 1][x] && c == modules[y + 1][x + 1]) {
                    result += PENALTY_N2
                }
            }

            // Rule 3: finder-like 1:1:3:1:1 pattern with 4 light modules on either side.
            for (y in 0 until size) result += finderLikePenalty { modules[y].getOrElse(it) { false } }
            for (x in 0 until size) result += finderLikePenalty { if (it in 0 until size) modules[it][x] else false }

            // Rule 4: dark/light balance.
            var dark = 0
            for (row in modules) for (m in row) if (m) dark++
            val total = size * size
            val k = (abs(dark * 20 - total * 10) + total - 1) / total - 1
            result += k * PENALTY_N4
            return result
        }

        private inline fun runPenalty(get: (Int) -> Boolean): Int {
            var penalty = 0
            var runColor = get(0)
            var runLen = 1
            for (i in 1 until size) {
                val c = get(i)
                if (c == runColor) {
                    runLen++
                } else {
                    if (runLen >= 5) penalty += PENALTY_N1 + (runLen - 5)
                    runColor = c
                    runLen = 1
                }
            }
            if (runLen >= 5) penalty += PENALTY_N1 + (runLen - 5)
            return penalty
        }

        /** [get] must return false (light) for out-of-range indices. */
        private inline fun finderLikePenalty(get: (Int) -> Boolean): Int {
            var penalty = 0
            for (i in 0..size - 7) {
                if (get(i) && !get(i + 1) && get(i + 2) && get(i + 3) && get(i + 4) && !get(i + 5) && get(i + 6)) {
                    val lightBefore = (1..4).none { get(i - it) }
                    val lightAfter = (7..10).none { get(i + it) }
                    if (lightBefore || lightAfter) penalty += PENALTY_N3
                }
            }
            return penalty
        }

        private companion object {
            const val PENALTY_N1 = 3
            const val PENALTY_N2 = 3
            const val PENALTY_N3 = 40
            const val PENALTY_N4 = 10
        }
    }
}

private fun bit(x: Int, i: Int): Boolean = ((x ushr i) and 1) != 0

/** Reed–Solomon over GF(2^8) with primitive polynomial x^8 + x^4 + x^3 + x^2 + 1 (0x11D). */
private object ReedSolomon {

    fun computeDivisor(degree: Int): IntArray {
        require(degree in 1..255)
        val result = IntArray(degree)
        result[degree - 1] = 1
        var root = 1
        for (i in 0 until degree) {
            for (j in 0 until degree) {
                result[j] = multiply(result[j], root)
                if (j + 1 < degree) result[j] = result[j] xor result[j + 1]
            }
            root = multiply(root, 0x02)
        }
        return result
    }

    fun computeRemainder(data: IntArray, divisor: IntArray): IntArray {
        val result = IntArray(divisor.size)
        for (b in data) {
            val factor = b xor result[0]
            result.copyInto(result, 0, 1, result.size)
            result[result.size - 1] = 0
            for (i in divisor.indices) result[i] = result[i] xor multiply(divisor[i], factor)
        }
        return result
    }

    private fun multiply(x: Int, y: Int): Int {
        var z = 0
        for (i in 7 downTo 0) {
            z = (z shl 1) xor ((z ushr 7) * 0x11D)
            z = z xor (((y ushr i) and 1) * x)
        }
        return z
    }
}

