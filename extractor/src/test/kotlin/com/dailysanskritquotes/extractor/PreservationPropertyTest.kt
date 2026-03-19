package com.dailysanskritquotes.extractor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

/**
 * Preservation Property Tests
 *
 * These tests lock down the existing CORRECT behavior of isHarvardKyoto and stripHkPrefix
 * on the UNFIXED code. They must PASS before the fix is applied, and continue to PASS after.
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4
 */
class PreservationPropertyTest {

    companion object {
        private lateinit var stripHkPrefixMethod: Method
        private lateinit var isHarvardKyotoMethod: Method

        @JvmStatic
        @BeforeAll
        fun setup() {
            val clazz = QuoteBlockExtractor::class.java

            stripHkPrefixMethod = clazz.getDeclaredMethod("stripHkPrefix", String::class.java)
            stripHkPrefixMethod.isAccessible = true

            isHarvardKyotoMethod = clazz.getDeclaredMethod("isHarvardKyoto", String::class.java)
            isHarvardKyotoMethod.isAccessible = true
        }

        private fun callStripHkPrefix(text: String): String {
            return stripHkPrefixMethod.invoke(QuoteBlockExtractor, text) as String
        }

        private fun callIsHarvardKyoto(text: String): Boolean {
            return isHarvardKyotoMethod.invoke(QuoteBlockExtractor, text) as Boolean
        }
    }

    /**
     * Test 1: Genuine HK detection — line with multiple [a-z][A-Z] transitions
     *
     * "nItidoShAH mRutyuH prIyate" has many HK transitions (nI, oS, hA, AH, uH, rI).
     * isHarvardKyoto must return true for genuine HK lines.
     *
     * Validates: Requirements 3.1
     */
    @Test
    @DisplayName("isHarvardKyoto returns true for genuine HK line with multiple transitions")
    fun isHarvardKyoto_genuineHK_multipleTransitions() {
        val input = "nItidoShAH mRutyuH prIyate"
        val result = callIsHarvardKyoto(input)
        assertTrue(
            result,
            "isHarvardKyoto should return true for genuine HK transliteration with multiple [a-z][A-Z] transitions"
        )
    }

    /**
     * Test 2: Genuine HK detection 2 — another HK line
     *
     * "dharmaH artha kAma mokShaH" has transitions (aH, kA, kS, aH).
     * isHarvardKyoto must return true.
     *
     * Validates: Requirements 3.1
     */
    @Test
    @DisplayName("isHarvardKyoto returns true for genuine HK line dharmaH artha kAma mokShaH")
    fun isHarvardKyoto_genuineHK_dharmaLine() {
        val input = "dharmaH artha kAma mokShaH"
        val result = callIsHarvardKyoto(input)
        assertTrue(
            result,
            "isHarvardKyoto should return true for genuine HK transliteration line"
        )
    }

    /**
     * Test 3: Mixed HK+English stripping — genuine HK prefix is stripped
     *
     * "nItidoShAH mRutyuH The wise seek truth" has HK words followed by English.
     * stripHkPrefix should strip the HK prefix and return "The wise seek truth".
     *
     * Validates: Requirements 3.2
     */
    @Test
    @DisplayName("stripHkPrefix strips genuine HK prefix from mixed HK+English line")
    fun stripHkPrefix_genuineHKPrefix_stripsCorrectly() {
        val input = "nItidoShAH mRutyuH The wise seek truth"
        val result = callStripHkPrefix(input)
        assertEquals(
            "The wise seek truth",
            result,
            "stripHkPrefix should strip genuine HK prefix and return the English portion"
        )
    }

    /**
     * Test 4: Danda-based detection — lines with dandas are classified as HK
     *
     * Lines containing Devanagari dandas (।, ॥) should be classified as HK
     * regardless of other content.
     *
     * Validates: Requirements 3.4
     */
    @Test
    @DisplayName("isHarvardKyoto returns true for lines containing dandas")
    fun isHarvardKyoto_dandaPresent_returnsTrue() {
        val input = "some text ।"
        val result = callIsHarvardKyoto(input)
        assertTrue(
            result,
            "isHarvardKyoto should return true for lines containing Devanagari dandas"
        )
    }

    /**
     * Test 5: Single-sentence English passthrough — lowercase-starting sentence unchanged
     *
     * A sentence starting with lowercase and containing no capitalized words at position > 0
     * that would trigger stripping should pass through stripHkPrefix unchanged.
     *
     * Validates: Requirements 3.3
     */
    @Test
    @DisplayName("stripHkPrefix returns lowercase-starting English sentence unchanged")
    fun stripHkPrefix_lowercaseEnglish_passthrough() {
        val input = "the quick brown fox jumps over the lazy dog"
        val result = callStripHkPrefix(input)
        assertEquals(
            input,
            result,
            "stripHkPrefix should return a lowercase-starting English sentence unchanged"
        )
    }
}
