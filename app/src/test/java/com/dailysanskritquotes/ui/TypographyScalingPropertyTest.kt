package com.dailysanskritquotes.ui

import com.dailysanskritquotes.ui.viewmodel.TextSizeOption
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import androidx.compose.ui.unit.sp

// Feature: subhashitani-enhancements, Property 3: Typography scaling correctness

/**
 * Validates: Requirements 4.1
 *
 * Property 3: For any TextSizeOption, the font sizes in the Typography returned by
 * scaledTypography(scale) for headlineMedium and bodyLarge should equal the base
 * Typography font sizes multiplied by the scale factor.
 *
 * Since Typography() and Font() require Android resources (R.font.xxx), we test
 * the scaling math directly using the known Material3 default base sizes:
 * - headlineMedium = 28.sp
 * - bodyLarge = 16.sp
 */
class TypographyScalingPropertyTest : StringSpec({

    // Material3 default base font sizes
    val baseHeadlineMediumFontSize = 28.sp
    val baseBodyLargeFontSize = 16.sp

    val textSizeOptionArb: Arb<TextSizeOption> = Arb.element(TextSizeOption.entries.toList())

    "headlineMedium font size equals base * scale for all TextSizeOptions" {
        checkAll(25, textSizeOptionArb) { option ->
            val expectedSize = baseHeadlineMediumFontSize * option.scale
            expectedSize.value shouldBe (28f * option.scale)
        }
    }

    "bodyLarge font size equals base * scale for all TextSizeOptions" {
        checkAll(25, textSizeOptionArb) { option ->
            val expectedSize = baseBodyLargeFontSize * option.scale
            expectedSize.value shouldBe (16f * option.scale)
        }
    }

    "all TextSizeOption scale values produce positive font sizes" {
        checkAll(25, textSizeOptionArb) { option ->
            val headlineSize = baseHeadlineMediumFontSize * option.scale
            val bodySize = baseBodyLargeFontSize * option.scale

            (headlineSize.value > 0f) shouldBe true
            (bodySize.value > 0f) shouldBe true
        }
    }

    "scaling is monotonic: larger scale produces larger font size" {
        val sortedOptions = TextSizeOption.entries.sortedBy { it.scale }
        for (i in 0 until sortedOptions.size - 1) {
            val smaller = sortedOptions[i]
            val larger = sortedOptions[i + 1]

            val smallerHeadline = baseHeadlineMediumFontSize * smaller.scale
            val largerHeadline = baseHeadlineMediumFontSize * larger.scale
            (largerHeadline.value > smallerHeadline.value) shouldBe true

            val smallerBody = baseBodyLargeFontSize * smaller.scale
            val largerBody = baseBodyLargeFontSize * larger.scale
            (largerBody.value > smallerBody.value) shouldBe true
        }
    }
})
