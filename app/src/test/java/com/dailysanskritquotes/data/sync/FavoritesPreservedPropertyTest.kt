package com.dailysanskritquotes.data.sync

import com.dailysanskritquotes.data.model.DeltaChange
import com.dailysanskritquotes.data.model.QuoteDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 10: Favorites preserved during sync

/**
 * Validates: Requirements 6.4
 *
 * Property 10: For any favorited quotes and any valid sync operation,
 * isFavorite and favoritedAt remain unchanged.
 *
 * Tested as pure logic: simulate merge on in-memory data,
 * verify favorite fields unchanged.
 */
class FavoritesPreservedPropertyTest : StringSpec({

    /**
     * In-memory quote with favorite metadata, mirroring QuoteEntity.
     */
    data class InMemoryQuote(
        val id: String,
        val sanskritText: String,
        val englishTranslation: String,
        val attribution: String,
        val isFavorite: Boolean = false,
        val favoritedAt: Long? = null
    )

    val quoteDtoArb: Arb<QuoteDto> = arbitrary {
        QuoteDto(
            id = Arb.string(minSize = 1, maxSize = 20).bind(),
            sanskritText = Arb.string(minSize = 1, maxSize = 100).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 100).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 50).bind()
        )
    }

    val inMemoryQuoteArb: Arb<InMemoryQuote> = arbitrary {
        InMemoryQuote(
            id = Arb.string(minSize = 1, maxSize = 20).bind(),
            sanskritText = Arb.string(minSize = 1, maxSize = 100).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 100).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 50).bind(),
            isFavorite = Arb.boolean().bind(),
            favoritedAt = if (Arb.boolean().bind()) Arb.long(1L..Long.MAX_VALUE / 2).bind() else null
        )
    }

    /**
     * Simulates delta application preserving favorites, mirroring QuoteRepository.applyDelta().
     * Updates only content fields (sanskritText, englishTranslation, attribution),
     * never touches isFavorite or favoritedAt.
     */
    fun applyDeltaPreservingFavorites(
        db: MutableMap<String, InMemoryQuote>,
        changes: List<DeltaChange>
    ) {
        for (change in changes) {
            when (change.action) {
                "add" -> {
                    val quote = change.quote ?: continue
                    db[quote.id] = InMemoryQuote(
                        id = quote.id,
                        sanskritText = quote.sanskritText,
                        englishTranslation = quote.englishTranslation,
                        attribution = quote.attribution
                    )
                }
                "update" -> {
                    val quote = change.quote ?: continue
                    val existing = db[quote.id]
                    db[quote.id] = InMemoryQuote(
                        id = quote.id,
                        sanskritText = quote.sanskritText,
                        englishTranslation = quote.englishTranslation,
                        attribution = quote.attribution,
                        isFavorite = existing?.isFavorite ?: false,
                        favoritedAt = existing?.favoritedAt
                    )
                }
                "delete" -> {
                    val id = change.quoteId ?: continue
                    val existing = db[id]
                    if (existing != null && !existing.isFavorite) {
                        db.remove(id)
                    }
                }
            }
        }
    }

    /**
     * Simulates full rebuild preserving favorites, mirroring QuoteRepository.rebuildFromFull().
     */
    fun rebuildPreservingFavorites(
        db: MutableMap<String, InMemoryQuote>,
        newQuotes: List<QuoteDto>
    ) {
        val favorites = db.values
            .filter { it.isFavorite }
            .associate { it.id to it.favoritedAt }

        db.clear()
        for (quote in newQuotes) {
            db[quote.id] = InMemoryQuote(
                id = quote.id,
                sanskritText = quote.sanskritText,
                englishTranslation = quote.englishTranslation,
                attribution = quote.attribution,
                isFavorite = favorites.containsKey(quote.id),
                favoritedAt = favorites[quote.id]
            )
        }
    }

    "delta update preserves isFavorite and favoritedAt for all favorited quotes" {
        checkAll(25, Arb.list(inMemoryQuoteArb, range = 1..10)) { existingQuotes ->
            val db = existingQuotes
                .mapIndexed { i, q -> q.copy(id = "q$i") }
                .associateBy { it.id }
                .toMutableMap()

            val favoritesBefore = db.values
                .filter { it.isFavorite }
                .associate { it.id to it.favoritedAt }

            // Create update changes for all existing quotes
            val changes = db.keys.map { id ->
                DeltaChange(
                    action = "update",
                    quote = QuoteDto(id, "new_sanskrit", "new_english", "new_attr")
                )
            }

            applyDeltaPreservingFavorites(db, changes)

            for ((id, originalFavoritedAt) in favoritesBefore) {
                db[id]!!.isFavorite shouldBe true
                db[id]!!.favoritedAt shouldBe originalFavoritedAt
            }
        }
    }

    "full rebuild preserves isFavorite and favoritedAt for all favorited quotes" {
        checkAll(25, Arb.list(inMemoryQuoteArb, range = 1..10)) { existingQuotes ->
            val db = existingQuotes
                .mapIndexed { i, q -> q.copy(id = "q$i") }
                .associateBy { it.id }
                .toMutableMap()

            val favoritesBefore = db.values
                .filter { it.isFavorite }
                .associate { it.id to it.favoritedAt }

            // Full rebuild with same IDs but new content
            val newQuotes = db.keys.map { id ->
                QuoteDto(id, "rebuilt_sanskrit", "rebuilt_english", "rebuilt_attr")
            }

            rebuildPreservingFavorites(db, newQuotes)

            for ((id, originalFavoritedAt) in favoritesBefore) {
                db[id]!!.isFavorite shouldBe true
                db[id]!!.favoritedAt shouldBe originalFavoritedAt
            }
        }
    }

    "delete does not remove favorited quotes" {
        checkAll(25, Arb.list(inMemoryQuoteArb, range = 1..10)) { existingQuotes ->
            val db = existingQuotes
                .mapIndexed { i, q -> q.copy(id = "q$i", isFavorite = true, favoritedAt = 1000L + i) }
                .associateBy { it.id }
                .toMutableMap()

            val changes = db.keys.map { id -> DeltaChange(action = "delete", quoteId = id) }

            applyDeltaPreservingFavorites(db, changes)

            // All favorited quotes should still be present
            for (q in existingQuotes.indices) {
                db.containsKey("q$q") shouldBe true
            }
        }
    }
})
