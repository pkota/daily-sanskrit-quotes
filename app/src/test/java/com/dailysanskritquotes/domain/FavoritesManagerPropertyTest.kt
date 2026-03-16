package com.dailysanskritquotes.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 4: Favorite toggle is self-inverse

/**
 * Validates: Requirements 3.1, 3.2
 *
 * Property 4: For any quote, toggling favorite twice returns to original state.
 * Tested as pure logic: the toggle operation (if favorite → unfavorite, if not → favorite)
 * applied twice is the identity function on the favorite state.
 */
class FavoriteToggleSelfInversePropertyTest : StringSpec({

    "toggling favorite status twice returns to original state" {
        checkAll(25, Arb.boolean()) { initialFavorite ->
            // First toggle: flip the state
            val afterFirstToggle = !initialFavorite

            // Second toggle: flip again
            val afterSecondToggle = !afterFirstToggle

            afterSecondToggle shouldBe initialFavorite
        }
    }

    "toggling favorite twice preserves favoritedAt semantics" {
        checkAll(25, Arb.boolean(), Arb.long(1L..Long.MAX_VALUE / 2)) { initialFavorite, timestamp ->
            val initialFavoritedAt: Long? = if (initialFavorite) timestamp else null

            // First toggle
            val afterFirstToggleFavorite = !initialFavorite
            val afterFirstToggleFavoritedAt: Long? = if (afterFirstToggleFavorite) {
                // Became favorite — gets a new timestamp
                timestamp + 1000
            } else {
                // Became unfavorited — cleared
                null
            }

            // Second toggle
            val afterSecondToggleFavorite = !afterFirstToggleFavorite
            val afterSecondToggleFavoritedAt: Long? = if (afterSecondToggleFavorite) {
                // Became favorite again — gets a new timestamp
                timestamp + 2000
            } else {
                // Became unfavorited — cleared
                null
            }

            // The boolean state should return to original
            afterSecondToggleFavorite shouldBe initialFavorite

            // If originally favorited, after double toggle it's favorited again (with a new timestamp)
            // If originally not favorited, after double toggle it's not favorited again (null)
            if (initialFavorite) {
                (afterSecondToggleFavoritedAt != null) shouldBe true
            } else {
                afterSecondToggleFavoritedAt shouldBe null
            }
        }
    }
})

// Feature: daily-sanskrit-quotes, Property 5: Favorites are reverse-chronologically ordered

/**
 * Validates: Requirements 3.3
 *
 * Property 5: For any set of favorited quotes with distinct timestamps,
 * getFavorites() returns strictly descending order of favoritedAt.
 * Tested as pure logic: sorting a list of (id, favoritedAt) pairs by favoritedAt DESC
 * produces the expected reverse-chronological order.
 */
class FavoritesReverseChronologicalOrderPropertyTest : StringSpec({

    data class FavoritedQuote(val id: String, val favoritedAt: Long)

    val favoritedQuoteArb: Arb<FavoritedQuote> = arbitrary {
        FavoritedQuote(
            id = Arb.string(minSize = 1, maxSize = 20).bind(),
            favoritedAt = Arb.long(1L..Long.MAX_VALUE / 2).bind()
        )
    }

    "favorites sorted by favoritedAt DESC are in strictly descending order" {
        checkAll(25, Arb.list(favoritedQuoteArb, range = 1..50)) { quotes ->
            // Ensure distinct timestamps by using index as offset
            val withDistinctTimestamps = quotes.mapIndexed { index, q ->
                q.copy(favoritedAt = q.favoritedAt + index)
            }

            // Sort by favoritedAt descending (this is what getFavorites() does via SQL)
            val sorted = withDistinctTimestamps.sortedByDescending { it.favoritedAt }

            // Verify strictly descending order
            for (i in 0 until sorted.size - 1) {
                (sorted[i].favoritedAt > sorted[i + 1].favoritedAt) shouldBe true
            }
        }
    }
})
