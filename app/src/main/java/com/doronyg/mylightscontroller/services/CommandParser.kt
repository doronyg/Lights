package com.doronyg.mylightscontroller.services

import com.doronyg.mylightscontroller.utils.Utils

// Represents the type of command detected from voice
enum class CommandType {
    TURN_ON,
    TURN_OFF,
    DIM,
    UNKNOWN
}

// Represents the parsed result from the voice input
data class ParsedVoiceOutput(
    val commandType: CommandType,  // What action the user wants
    val deviceText: String?,       // What the user said (e.g., "kitchen light")
    val brightness: Int? = null    // Optional brightness (1-100)
)

object CommandParser {
    val TURN_ON_WORDS_LIST = listOf(
        "turn on", "switch on", "lights on", "power on",
        "activate", "enable", "start"
    )

    val TURN_OFF_WORDS_LIST = listOf(
        "turn off", "switch off", "lights off", "power off",
        "kill the lights", "shut down", "deactivate", "disable", "stop"
    )

    // Expanded to catch natural phrasing like "set the light to 50"
    val DIM_WORDS_LIST = listOf(
        "dim", "brighten", "set brightness", "raise", "lower",
        "adjust", "set", "make", "change", "to", "percent", "percentage"
    )

    val STOP_WORDS_LIST = listOf("cancel", "stop", "abort", "nevermind", "never mind", "undo")

    val NUMBER_WORDS_LIST = generateVoiceInputMap()

    fun parseText(command: String, haLightsNames: List<String>): ParsedVoiceOutput? {
        // Normalize the incoming voice command
        val normalizedText = normalize(command)

        // 0. Quick Exit
        if (STOP_WORDS_LIST.any { "\\b$it\\b".toRegex().containsMatchIn(normalizedText) }) {
            return ParsedVoiceOutput(CommandType.UNKNOWN, null)
        }

        // 1. Extract Device FIRST (using original text to avoid "Bedroom 2" -> "Bedroom two" mismatch)
        // We sort names by length descending so "Kitchen Table Light" matches before "Kitchen"
        val device = haLightsNames
            .sortedByDescending { it.length }
            .find { normalize(it) in normalizedText }

        // 2. Normalize numbers for intent and brightness parsing
        val processedText = replaceNumberWords(normalizedText)

        // 3. Determine Intent with Word Boundaries (avoids "dim" matching "dimmer")
        val commandType = when {
            containsWord(processedText, TURN_ON_WORDS_LIST) -> CommandType.TURN_ON
            containsWord(processedText, TURN_OFF_WORDS_LIST) -> CommandType.TURN_OFF
            containsWord(processedText, DIM_WORDS_LIST) -> CommandType.DIM
            else -> CommandType.UNKNOWN
        }

        // ---- Step 4: Extract brightness (with "250" logic) ----
        val rawDigits = Regex("""\b(\d{1,3})\b""")
            .find(processedText)
            ?.groupValues
            ?.get(1)

        var brightness = rawDigits?.toIntOrNull()

        if (brightness != null) {
            // Check if STT combined "to" (2) with a number (e.g., 250, 220, 280)
            if (brightness > 100 && rawDigits!!.startsWith("2")) {
                // Strip the '2' and parse the remainder
                val corrected = rawDigits.substring(1).toIntOrNull()
                if (corrected != null && corrected in 1..100) {
                    brightness = corrected
                }
            }

            // Final safety clamp
            brightness = brightness.coerceIn(1, 100)
        }

        return if (commandType != CommandType.UNKNOWN || device != null) {
            val parsedVoiceOutput = ParsedVoiceOutput(commandType, device, brightness)
            Utils.log("parseText() command -> $parsedVoiceOutput")
            parsedVoiceOutput
        } else null
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            // Remove everything that isn't a letter, digit, or space
            .replace(Regex("[^a-z0-9\\s]"), "")
            // Replace multiple spaces with a single space
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // Helper to check for whole words only
    private fun containsWord(text: String, words: List<String>): Boolean {
        return words.any { "\\b$it\\b".toRegex().containsMatchIn(text) }
    }

    /**
     * Replaces number words in a string with their digit equivalents.
     * Sorts keys by length to ensure compound numbers (e.g., "twenty-five")
     * are matched before single words (e.g., "twenty").
     */
    fun replaceNumberWords(text: String): String {
        var result = text.lowercase()

        // Sort keys by word count or character length descending
        val sortedKeys = NUMBER_WORDS_LIST.keys.sortedByDescending { it.length }

        for (word in sortedKeys) {
            val value = NUMBER_WORDS_LIST[word].toString()

            // Use Regex with word boundaries (\b) so "one" doesn't
            // accidentally replace part of "someone" or "lonely"
            val regex = "\\b$word\\b".toRegex()
            result = result.replace(regex, value)
        }

        return result
    }

    fun generateVoiceInputMap(): Map<String, Int> {
        val units = arrayOf("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
        val tens = arrayOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

        val numberMap = mutableMapOf<String, Int>()

        for (i in 0..100) {
            when {
                i == 0 -> numberMap["zero"] = 0
                i == 100 -> {
                    numberMap["one hundred"] = 100
                    numberMap["a hundred"] = 100 // Common in speech
                }
                i < 20 -> numberMap[units[i]] = i
                else -> {
                    val tenPart = tens[i / 10]
                    val unitPart = units[i % 10]

                    if (unitPart.isEmpty()) {
                        numberMap[tenPart] = i
                    } else {
                        // Speech engines vary, so we map both styles
                        numberMap["$tenPart-$unitPart"] = i
                        numberMap["$tenPart $unitPart"] = i
                    }
                }
            }
        }
        return numberMap
    }

}