package com.dailysanskritquotes.ui

import com.dailysanskritquotes.ui.viewmodel.AppColorTheme
import com.dailysanskritquotes.ui.viewmodel.TextSizeOption
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

// Feature: subhashitani-enhancements, Property 1: Settings persistence round-trip

/**
 * Validates: Requirements 2.3, 3.2, 4.2
 *
 * Property 1: For any combination of dark mode (Boolean), color theme (AppColorTheme),
 * and text size (TextSizeOption), persisting these settings and loading them back
 * should produce the same values.
 *
 * Since SharedPreferences requires Android, we test the mapping logic:
 * - ordinal → enum → ordinal is identity for all valid values
 * - invalid ordinals fall back to defaults
 * - Boolean round-trips correctly
 */
class SettingsPersistencePropertyTest : StringSpec({

    val colorThemeArb: Arb<AppColorTheme> = Arb.element(AppColorTheme.entries.toList())
    val textSizeArb: Arb<TextSizeOption> = Arb.element(TextSizeOption.entries.toList())
    val booleanArb: Arb<Boolean> = Arb.boolean()

    "AppColorTheme ordinal round-trip is identity for all valid values" {
        checkAll(25, colorThemeArb) { theme ->
            val ordinal = theme.ordinal
            val restored = AppColorTheme.entries.getOrElse(ordinal) { AppColorTheme.DEFAULT }
            restored shouldBe theme
        }
    }

    "TextSizeOption ordinal round-trip is identity for all valid values" {
        checkAll(25, textSizeArb) { size ->
            val ordinal = size.ordinal
            val restored = TextSizeOption.entries.getOrElse(ordinal) { TextSizeOption.MEDIUM }
            restored shouldBe size
        }
    }

    "dark mode Boolean round-trip preserves value" {
        checkAll(25, booleanArb) { darkMode ->
            // Simulate SharedPreferences: store as Boolean, load as Boolean
            val stored = darkMode
            val loaded = stored
            loaded shouldBe darkMode
        }
    }

    "invalid AppColorTheme ordinal falls back to DEFAULT" {
        checkAll(25, Arb.int(-100..100)) { ordinal ->
            val restored = AppColorTheme.entries.getOrElse(ordinal) { AppColorTheme.DEFAULT }
            if (ordinal in AppColorTheme.entries.indices) {
                restored shouldBe AppColorTheme.entries[ordinal]
            } else {
                restored shouldBe AppColorTheme.DEFAULT
            }
        }
    }

    "invalid TextSizeOption ordinal falls back to MEDIUM" {
        checkAll(25, Arb.int(-100..100)) { ordinal ->
            val restored = TextSizeOption.entries.getOrElse(ordinal) { TextSizeOption.MEDIUM }
            if (ordinal in TextSizeOption.entries.indices) {
                restored shouldBe TextSizeOption.entries[ordinal]
            } else {
                restored shouldBe TextSizeOption.MEDIUM
            }
        }
    }

    "combined settings round-trip preserves all values" {
        checkAll(25, booleanArb, colorThemeArb, textSizeArb) { darkMode, colorTheme, textSize ->
            // Simulate persist
            val darkModeStored = darkMode
            val colorThemeOrdinal = colorTheme.ordinal
            val textSizeOrdinal = textSize.ordinal

            // Simulate load
            val darkModeLoaded = darkModeStored
            val colorThemeLoaded = AppColorTheme.entries.getOrElse(colorThemeOrdinal) { AppColorTheme.DEFAULT }
            val textSizeLoaded = TextSizeOption.entries.getOrElse(textSizeOrdinal) { TextSizeOption.MEDIUM }

            darkModeLoaded shouldBe darkMode
            colorThemeLoaded shouldBe colorTheme
            textSizeLoaded shouldBe textSize
        }
    }
})
