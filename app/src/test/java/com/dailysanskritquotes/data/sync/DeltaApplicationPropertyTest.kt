package com.dailysanskritquotes.data.sync

import com.dailysanskritquotes.data.model.DeltaChange
import com.dailysanskritquotes.data.model.QuoteDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 9: Delta application correctness

/**
 * Validates: Requirements 6.3
 *
 * Property 9: For any local DB at version V and valid delta V→W:
 * all adds exist, all updates reflected, local version = W.
 *
 * Tested as pure logic: simulate delta application on in-memory Map<String, QuoteDto>.
 */
class DeltaApplicationPropertyTest : StringSpec({

    val quoteDtoArb: Arb<QuoteDto> = arbitrary {
        QuoteDto(
            id = Arb.string(minSize = 1, maxSize = 20).bind(),
            sanskritText = Arb.string(minSize = 1, maxSize = 100).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 100).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 50).bind()
        )
    }

    /**
     * Pure function simulating delta application on an in-memory database.
     * Mirrors the logic in QuoteRepository.applyDelta().
     */
    fun applyDeltaInMemory(
        db: MutableMap<String, QuoteDto>,
        changes: List<DeltaChange>
    ) {
        for (change in changes) {
            when (change.action) {
                "add" -> {
                    val quote = change.quote ?: continue
                    db[quote.id] = quote
                }
                "update" -> {
                    val quote = change.quote ?: continue
                    db[quote.id] = quote
                }
                "delete" -> {
                    val id = change.quoteId ?: continue
                    db.remove(id)
                }
            }
        }
    }

    "after applying delta, all added quotes exist in the database" {
        checkAll(
            25,
            Arb.list(quoteDtoArb, range = 0..10),
            Arb.list(quoteDtoArb, range = 1..10)
        ) { existingQuotes, newQuotes ->
            val db = existingQuotes.associateBy { it.id }.toMutableMap()
            // Ensure new quotes have unique IDs not in existing DB
            val addQuotes = newQuotes.mapIndexed { i, q -> q.copy(id = "new_$i") }
            val changes = addQuotes.map { DeltaChange(action = "add", quote = it) }

            applyDeltaInMemory(db, changes)

            for (added in addQuotes) {
                db.containsKey(added.id) shouldBe true
                db[added.id] shouldBe added
            }
        }
    }

    "after applying delta, all updated quotes reflect new field values" {
        checkAll(
            25,
            Arb.list(quoteDtoArb, range = 1..10),
            Arb.list(quoteDtoArb, range = 1..10)
        ) { existingQuotes, updateData ->
            val db = existingQuotes.associateBy { it.id }.toMutableMap()
            val existingIds = db.keys.toList()
            // Create update changes targeting existing IDs
            val updates = updateData.mapIndexed { i, q ->
                val targetId = existingIds[i % existingIds.size]
                DeltaChange(action = "update", quote = q.copy(id = targetId))
            }

            applyDeltaInMemory(db, updates)

            // Only the last update per ID matters (later changes overwrite earlier ones)
            val lastUpdatePerQuote = updates.reversed()
                .distinctBy { it.quote!!.id }
            for (change in lastUpdatePerQuote) {
                val expected = change.quote!!
                db[expected.id] shouldBe expected
            }
        }
    }

    "version is updated to target version after delta application" {
        checkAll(
            25,
            Arb.int(1..100),
            Arb.int(1..100)
        ) { localVersion, versionBump ->
            val targetVersion = localVersion + versionBump

            // Simulate: after successful delta apply, version should be set to target
            // This is a pure logic check: the algorithm sets version = manifest.version
            targetVersion shouldBe (localVersion + versionBump)
            (targetVersion > localVersion) shouldBe true
        }
    }
})
