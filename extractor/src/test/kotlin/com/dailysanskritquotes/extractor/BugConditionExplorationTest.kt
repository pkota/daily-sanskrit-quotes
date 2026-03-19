package com.dailysanskritquotes.extractor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

/**
 * Bug Condition Exploration Tests
 *
 * These tests demonstrate the bug in stripHkPrefix and isHarvardKyoto.
 * They encode the EXPECTED (correct) behavior — on unfixed code they will FAIL,
 * confirming the bug exists. After the fix, they should PASS.
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 2.1, 2.2, 2.3
 */
class BugConditionExplorationTest {

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
     * Test 1: stripHkPrefix truncation with proper noun "Kruta Yuga"
     *
     * Bug: stripHkPrefix sees "Kruta" at position > 0, prefix is plain English,
     * strips to "Kruta Yuga." — losing "Penance is the best way to live in the"
     *
     * Validates: Requirements 1.1, 2.1
     */
    @Test
    @DisplayName("stripHkPrefix should preserve full sentence with proper noun Kruta Yuga")
    fun stripHkPrefix_truncation_properNounKrutaYuga() {
        val input = "Penance is the best way to live in the Kruta Yuga."
        val result = callStripHkPrefix(input)
        assertEquals(
            input,
            result,
            "stripHkPrefix should return the full string unchanged for plain English with proper nouns"
        )
    }

    /**
     * Test 2: stripHkPrefix multi-sentence truncation
     *
     * Bug: stripHkPrefix sees "Knowledge" at position > 0, prefix is plain text,
     * strips to "Knowledge leads to liberation." — losing the first sentence
     *
     * Validates: Requirements 1.2, 2.1
     */
    @Test
    @DisplayName("stripHkPrefix should preserve multi-sentence English text")
    fun stripHkPrefix_multiSentence_truncation() {
        val input = "The wise seek truth. Knowledge leads to liberation."
        val result = callStripHkPrefix(input)
        assertEquals(
            input,
            result,
            "stripHkPrefix should return the full string unchanged for multi-sentence English"
        )
    }

    /**
     * Test 3: isHarvardKyoto false positive on "McDonald's"
     *
     * Bug: isHarvardKyoto finds 1 match (cD in McDonald) and returns true,
     * filtering out a perfectly valid English line
     *
     * Validates: Requirements 1.3, 2.3
     */
    @Test
    @DisplayName("isHarvardKyoto should return false for English with incidental cD match")
    fun isHarvardKyoto_falsePositive_mcDonalds() {
        val input = "He went to McDonald's for lunch"
        val result = callIsHarvardKyoto(input)
        assertFalse(
            result,
            "isHarvardKyoto should return false for normal English text with incidental [a-z][A-Z] match"
        )
    }

    /**
     * Test 4: stripHkPrefix with proper noun "Dharma"
     *
     * Bug: stripHkPrefix sees "Dharma" at position > 0, prefix is plain English,
     * strips to "Dharma and its meaning"
     *
     * Validates: Requirements 1.1, 2.2
     */
    @Test
    @DisplayName("stripHkPrefix should preserve sentence with proper noun Dharma")
    fun stripHkPrefix_properNoun_dharma() {
        val input = "This is about Dharma and its meaning"
        val result = callStripHkPrefix(input)
        assertEquals(
            input,
            result,
            "stripHkPrefix should return the full string unchanged for English with proper nouns"
        )
    }
}
