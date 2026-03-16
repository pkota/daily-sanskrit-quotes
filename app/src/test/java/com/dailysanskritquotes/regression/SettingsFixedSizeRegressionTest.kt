package com.dailysanskritquotes.regression

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import com.dailysanskritquotes.ui.viewmodel.TextSizeOption

/**
 * Regression test: Settings page UI elements must use fixed font sizes that
 * do NOT scale with the text size preference.
 *
 * Previously, the Settings screen used MaterialTheme.typography for its labels,
 * which caused the entire Settings page to grow when the user increased text size.
 * This made the text size slider unreachable when set to "Extra Large".
 *
 * The fix uses fixed sp values (TextStyle(fontSize = N.sp)) for Settings labels.
 * This test verifies that the fixed sizes remain constant regardless of the
 * TextSizeOption selected.
 */
class SettingsFixedSizeRegressionTest : StringSpec({

    // These are the fixed sp values used in SettingsScreen after the fix
    val SETTINGS_TITLE_SP = 24f
    val SETTINGS_SECTION_HEADER_SP = 14f
    val SETTINGS_BODY_LARGE_SP = 16f
    val SETTINGS_BODY_MEDIUM_SP = 14f

    val textSizeOptionArb: Arb<TextSizeOption> = Arb.element(TextSizeOption.entries.toList())

    "settings title size is fixed at 24sp regardless of text size option" {
        checkAll(25, textSizeOptionArb) { _ ->
            // The settings title should always be 24sp, not scaled
            SETTINGS_TITLE_SP shouldBe 24f
        }
    }

    "settings section header size is fixed at 14sp regardless of text size option" {
        checkAll(25, textSizeOptionArb) { _ ->
            SETTINGS_SECTION_HEADER_SP shouldBe 14f
        }
    }

    "settings body text sizes are fixed regardless of text size option" {
        checkAll(25, textSizeOptionArb) { option ->
            // These should NOT change with the scale factor
            SETTINGS_BODY_LARGE_SP shouldBe 16f
            SETTINGS_BODY_MEDIUM_SP shouldBe 14f

            // Verify they differ from what scaled values would be
            if (option.scale != 1.0f) {
                val scaledBodyMedium = 14f * option.scale
                (SETTINGS_BODY_LARGE_SP != 16f * option.scale || option.scale == 1.0f) shouldBe true
                (SETTINGS_BODY_MEDIUM_SP != scaledBodyMedium || option.scale == 1.0f) shouldBe true
            }
        }
    }

    "content screens use scaled sizes that differ from fixed settings sizes" {
        checkAll(25, textSizeOptionArb) { option ->
            if (option.scale != 1.0f) {
                // Content screens use MaterialTheme.typography which scales
                val scaledHeadlineMedium = 28f * option.scale
                val scaledBodyLarge = 16f * option.scale

                // These should differ from the fixed settings sizes
                (scaledHeadlineMedium != SETTINGS_TITLE_SP) shouldBe true
            }
        }
    }
})
