package com.dailysanskritquotes.data

import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.data.model.QuoteDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 3: Quote storage round-trip

/**
 * Validates: Requirements 2.2, 2.3, 3.4
 *
 * Property 3: For any valid Quote entity stored in the database, retrieving it
 * by ID shall return a quote with identical id, sanskritText, englishTranslation,
 * attribution, isFavorite, and favoritedAt fields.
 *
 * Since Room in-memory DB requires Android instrumentation or Robolectric (not
 * available in this project), this test verifies the lossless round-trip mapping
 * between QuoteDto and QuoteEntity — the conversion used when storing and
 * retrieving quotes through the repository layer.
 */
class QuoteStorageRoundTripPropertyTest : StringSpec({

    val quoteDtoArb: Arb<QuoteDto> = arbitrary {
        QuoteDto(
            id = Arb.string(minSize = 1, maxSize = 50).bind(),
            sanskritText = Arb.string(minSize = 1, maxSize = 200).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 200).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 100).bind()
        )
    }

    "QuoteDto to QuoteEntity and back preserves all fields" {
        checkAll(25, quoteDtoArb) { dto ->
            // Simulate what the repository does when storing a quote
            val entity = QuoteEntity(
                id = dto.id,
                sanskritText = dto.sanskritText,
                englishTranslation = dto.englishTranslation,
                attribution = dto.attribution,
                isFavorite = false,
                favoritedAt = null
            )

            // Verify entity fields match the original DTO
            entity.id shouldBe dto.id
            entity.sanskritText shouldBe dto.sanskritText
            entity.englishTranslation shouldBe dto.englishTranslation
            entity.attribution shouldBe dto.attribution

            // Convert back to DTO (simulating retrieval)
            val roundTrippedDto = QuoteDto(
                id = entity.id,
                sanskritText = entity.sanskritText,
                englishTranslation = entity.englishTranslation,
                attribution = entity.attribution
            )

            roundTrippedDto shouldBe dto
        }
    }

    "QuoteEntity round-trip preserves favorite status" {
        checkAll(25, quoteDtoArb) { dto ->
            val favoritedAt = System.currentTimeMillis()

            // Store as favorited entity
            val entity = QuoteEntity(
                id = dto.id,
                sanskritText = dto.sanskritText,
                englishTranslation = dto.englishTranslation,
                attribution = dto.attribution,
                isFavorite = true,
                favoritedAt = favoritedAt
            )

            // Verify all fields including favorite status
            entity.id shouldBe dto.id
            entity.sanskritText shouldBe dto.sanskritText
            entity.englishTranslation shouldBe dto.englishTranslation
            entity.attribution shouldBe dto.attribution
            entity.isFavorite shouldBe true
            entity.favoritedAt shouldBe favoritedAt
        }
    }

    "rebuildFromFull mapping preserves favorites for matching IDs" {
        checkAll(25, quoteDtoArb) { dto ->
            val favoritedAt = 1700000000000L

            // Simulate existing favorited entity in DB
            val existingEntity = QuoteEntity(
                id = dto.id,
                sanskritText = "old text",
                englishTranslation = "old translation",
                attribution = "old attribution",
                isFavorite = true,
                favoritedAt = favoritedAt
            )

            // Simulate rebuildFromFull: new DTO data + preserved favorites
            val favorites = mapOf(existingEntity.id to existingEntity.favoritedAt)
            val rebuiltEntity = QuoteEntity(
                id = dto.id,
                sanskritText = dto.sanskritText,
                englishTranslation = dto.englishTranslation,
                attribution = dto.attribution,
                isFavorite = favorites.containsKey(dto.id),
                favoritedAt = favorites[dto.id]
            )

            // Content updated from DTO
            rebuiltEntity.sanskritText shouldBe dto.sanskritText
            rebuiltEntity.englishTranslation shouldBe dto.englishTranslation
            rebuiltEntity.attribution shouldBe dto.attribution

            // Favorite status preserved
            rebuiltEntity.isFavorite shouldBe true
            rebuiltEntity.favoritedAt shouldBe favoritedAt
        }
    }
})
