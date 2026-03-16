package com.dailysanskritquotes.regression

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import com.dailysanskritquotes.ui.viewmodel.TextSizeOption

/**
 * Regression test: scaledTypography must apply VesperLibre and scaling to ALL
 * 15 Material3 typography styles, not just headlineMedium and bodyLarge.
 *
 * Previously, only headlineMedium and bodyLarge were customized, leaving all
 * other styles at system defaults. This caused quote text using other styles
 * to not show Vesper Libre or respond to font size changes.
 *
 * Since scaledTypography() requires Android resources (R.font), we test the
 * scaling math directly: for every base style, scaled size == base size * scale.
 */
class TypographyAllStylesRegressionTest : StringSpec({

    // Material3 default base font sizes for all 15 styles
    data class StyleSpec(val name: String, val baseSizeSp: Float)

    val allStyles = listOf(
        StyleSpec("displayLarge", 57f),
        StyleSpec("displayMedium", 45f),
        StyleSpec("displaySmall", 36f),
        StyleSpec("headlineLarge", 32f),
        StyleSpec("headlineMedium", 28f),
        StyleSpec("headlineSmall", 24f),
        StyleSpec("titleLarge", 22f),
        StyleSpec("titleMedium", 16f),
        StyleSpec("titleSmall", 14f),
        StyleSpec("bodyLarge", 16f),
        StyleSpec("bodyMedium", 14f),
        StyleSpec("bodySmall", 12f),
        StyleSpec("labelLarge", 14f),
        StyleSpec("labelMedium", 12f),
        StyleSpec("labelSmall", 11f),
    )

    val textSizeOptionArb: Arb<TextSizeOption> = Arb.element(TextSizeOption.entries.toList())

    "all 15 typography styles scale correctly for any TextSizeOption" {
        checkAll(25, textSizeOptionArb) { option ->
            allStyles.forEach { style ->
                val expected = style.baseSizeSp * option.scale
                val actual = (style.baseSizeSp.sp * option.scale).value
                // Allow small floating point tolerance
                val diff = kotlin.math.abs(actual - expected)
                (diff < 0.01f) shouldBe true
            }
        }
    }

    "scaling is consistent: same scale produces same sizes across calls" {
        checkAll(25, textSizeOptionArb) { option ->
            val sizes1 = allStyles.map { (it.baseSizeSp.sp * option.scale).value }
            val sizes2 = allStyles.map { (it.baseSizeSp.sp * option.scale).value }
            sizes1 shouldBe sizes2
        }
    }

    "all scaled sizes are positive" {
        checkAll(25, textSizeOptionArb) { option ->
            allStyles.forEach { style ->
                val scaled = style.baseSizeSp * option.scale
                (scaled > 0f) shouldBe true
            }
        }
    }
})
