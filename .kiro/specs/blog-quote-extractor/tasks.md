# Implementation Plan: Blog Quote Extractor

## Overview

Build the `:extractor` Gradle module as a standalone Kotlin/JVM CLI tool that parses exported Blogger HTML files, extracts Sanskrit quotes, generates IAST transliteration, deduplicates, assigns IDs, and outputs a `quotes-full.json` compatible with the Subhashitani app. Implementation follows the 10-stage pipeline defined in the design, building incrementally from project setup through each pipeline stage to final integration.

## Tasks

- [x] 1. Set up the `:extractor` Gradle module and project structure
  - [x] 1.1 Create the `extractor` module directory structure (`extractor/src/main/kotlin/com/dailysanskritquotes/extractor/` and `extractor/src/test/kotlin/com/dailysanskritquotes/extractor/`)
    - Create `extractor/build.gradle.kts` with JVM-only Kotlin plugin, Jsoup dependency, kotlinx.serialization, and Kotest Property test dependencies
    - Add `:extractor` to `settings.gradle.kts`
    - Configure dependency on the `app` module's `QuoteDto` and `QuoteFile` data models (or extract shared models)
    - _Requirements: 1.1, 1.2_

  - [x] 1.2 Create the `RawQuote` and `ParsedPost` data classes
    - Create `extractor/src/main/kotlin/com/dailysanskritquotes/extractor/model/RawQuote.kt`
    - Create `extractor/src/main/kotlin/com/dailysanskritquotes/extractor/model/ParsedPost.kt`
    - _Requirements: 3.1, 3.2, 3.3_

- [x] 2. Implement DirectoryScanner
  - [x] 2.1 Implement `DirectoryScanner.findHtmlFiles`
    - Recursively scan input directory for `.html` files
    - Return files sorted alphabetically by path
    - Return empty list if no `.html` files found
    - _Requirements: 1.5, 5.3_

  - [ ]* 2.2 Write property test for DirectoryScanner
    - **Property 1: Recursive HTML file discovery**
    - Generate random directory trees with mixed file extensions, verify only `.html` files returned and sorted alphabetically
    - **Validates: Requirements 1.5**

- [x] 3. Implement HtmlParser
  - [x] 3.1 Implement `HtmlParser.parse`
    - Use Jsoup to parse each HTML file
    - Extract post title, ordered body text segments, and Blogger labels from "Labels:" metadata section
    - Return `null` and log warning for unparseable files
    - Support UTF-8 encoded files
    - _Requirements: 2.1, 2.4, 2.5, 4.3_

  - [ ]* 3.2 Write unit tests for HtmlParser
    - Test parsing a sample Blogger HTML file with known structure
    - Test handling of malformed HTML (returns null, logs warning)
    - Test extraction of labels from "Labels:" section
    - _Requirements: 2.1, 2.4, 2.5_

- [x] 4. Implement QuoteBlockExtractor
  - [x] 4.1 Implement `QuoteBlockExtractor.extract`
    - Detect Devanagari text segments (Unicode range U+0900–U+097F) as the Sanskrit verse
    - Skip Harvard-Kyoto transliteration block
    - Extract English translation segment
    - Resolve attribution from Devanagari attribution line (prefixed with `- ` or `—`) or English attribution line; default to "Unknown" if absent
    - _Requirements: 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.6, 4.1, 4.2_

  - [ ]* 4.2 Write property test for QuoteBlockExtractor field extraction
    - **Property 2: Quote field extraction completeness**
    - Generate random Devanagari text and English translations, embed in HTML structure, verify extraction preserves original text
    - **Validates: Requirements 2.2, 3.1, 3.2, 4.1**

  - [ ]* 4.3 Write property test for attribution extraction
    - **Property 3: Attribution extraction from title**
    - Generate random `{Source} - {Topic}` titles, verify source portion extracted as attribution
    - **Validates: Requirements 2.3, 3.3, 4.2**

- [x] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implement DevanagariToIast transliterator
  - [x] 6.1 Implement `DevanagariToIast.transliterate`
    - Build lookup table mapping Devanagari Unicode codepoints to IAST characters
    - Handle vowels (independent and matra forms), consonants with implicit 'a', virama (halant), anusvara, visarga, chandrabindu
    - Handle danda (। → |), double danda (॥ → ||), Devanagari numerals (०-९ → 0-9)
    - Pass through non-Devanagari characters unchanged
    - _Requirements: 3.5, 3.6, 3.7_

  - [ ]* 6.2 Write property test for IAST transliteration output validity
    - **Property 4: Devanagari-to-IAST transliteration produces valid IAST**
    - Generate random Devanagari strings, verify output contains only valid IAST characters, punctuation, digits, and whitespace
    - **Validates: Requirements 3.5**

  - [ ]* 6.3 Write unit tests for DevanagariToIast edge cases
    - Test known transliterations (e.g., "धर्मो" → "dharmo", "रक्षति" → "rakṣati")
    - Test danda/double-danda, Devanagari numerals, virama at end of word, chandrabindu
    - _Requirements: 3.5, 3.7_

- [x] 7. Implement TagExtractor
  - [x] 7.1 Implement `TagExtractor.fromLabels`, `fromTranslation`, and `extract`
    - `fromLabels`: Filter out "Subhashita" (case-insensitive) and labels matching attribution, return up to 2
    - `fromTranslation`: Keyword-based thematic classification mapping English translation keywords to categories (Wisdom, Virtue, Education, Devotion, etc.), return up to 2
    - `extract`: Use `fromLabels` first, fall back to `fromTranslation` if no labels remain
    - _Requirements: 3.8, 4.3, 4.4, 4.5, 4.6, 4.7_

  - [ ]* 7.2 Write property test for tag filtering invariants
    - **Property 5: Tag filtering invariants**
    - Generate random label lists and attributions, verify output excludes "Subhashita", excludes attribution matches, and has at most 2 elements
    - **Validates: Requirements 3.8, 4.3, 4.4, 4.5, 4.7**

  - [ ]* 7.3 Write property test for fallback tag generation
    - **Property 6: Fallback tag generation**
    - Generate quotes with empty/fully-excluded labels, verify tags come from translation text and have at most 2 elements
    - **Validates: Requirements 4.6**

- [x] 8. Implement Deduplicator
  - [x] 8.1 Implement `Deduplicator.normalize` and `Deduplicator.deduplicate`
    - `normalize`: Trim whitespace and collapse consecutive whitespace to single space
    - `deduplicate`: Group by normalized `sanskritText`, keep first occurrence, merge tags (deduplicated union, max 2), log duplicates with source file info
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ]* 8.2 Write property test for deduplication first-occurrence retention
    - **Property 8: Deduplication keeps first occurrence**
    - Generate random quote lists with duplicates, verify first occurrence retained, each normalized text appears once, output length equals distinct count
    - **Validates: Requirements 6.1, 6.3**

  - [ ]* 8.3 Write property test for normalization idempotency
    - **Property 9: Text normalization is idempotent**
    - Generate random strings with varied whitespace, verify `normalize(normalize(x)) == normalize(x)`
    - **Validates: Requirements 6.2**

  - [ ]* 8.4 Write property test for tag merging across duplicates
    - **Property 10: Tag merging across duplicates**
    - Generate duplicate quotes with different tags, verify merged deduplicated union with at most 2 tags
    - **Validates: Requirements 6.5**

- [x] 9. Implement IdAssigner
  - [x] 9.1 Implement `IdAssigner.assignIds`
    - Assign sequential IDs in format `q0001`, `q0002`, etc.
    - Determine padding width from total count (4 digits for ≤9999, wider if needed)
    - Build `QuoteDto` instances with IAST transliteration via `DevanagariToIast`
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ]* 9.2 Write property test for ID format and sequential assignment
    - **Property 7: ID format and sequential assignment**
    - Generate random quote counts, verify IDs are `q0001` through `q{N}`, unique, contiguous, zero-padded to at least 4 digits
    - **Validates: Requirements 5.1, 5.3**

- [x] 10. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Implement JsonWriter with empty field omission
  - [x] 11.1 Implement `JsonWriter.write`
    - Create `QuoteFile(version = 1, quotes = quotes)` and serialize with kotlinx.serialization
    - Pretty-print with 4-space indent, UTF-8 encoding
    - Omit `transliteration` field when empty string, omit `tags` field when empty list
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [ ]* 11.2 Write property test for empty field omission
    - **Property 11: Empty field omission in JSON serialization**
    - Generate QuoteDtos with empty transliteration/tags, verify serialized JSON does not contain those keys
    - **Validates: Requirements 7.4, 7.5**

- [x] 12. Implement RoundTripValidator
  - [x] 12.1 Implement `RoundTripValidator.validate`
    - Parse generated JSON with `QuoteFileParser.parseQuoteFile`
    - Re-print with `QuoteFilePrinter.print`
    - Compare parsed `QuoteFile` objects for semantic equality
    - Return `Result.failure` with descriptive message on mismatch
    - _Requirements: 8.1, 8.2, 8.3_

  - [ ]* 12.2 Write property test for QuoteFile round-trip
    - **Property 12: QuoteFile round-trip**
    - Generate random QuoteFile instances, verify `parse(print(file))` equals original
    - **Validates: Requirements 7.1, 7.2, 8.1**

- [x] 13. Implement CLI entry point and wire pipeline together
  - [x] 13.1 Implement `main` function in `Main.kt`
    - Parse `--input <dir>` and `--output <file>` command-line arguments
    - Validate input directory exists and contains `.html` files (exit non-zero with error messages for failures)
    - Orchestrate the full pipeline: DirectoryScanner → HtmlParser → QuoteBlockExtractor → DevanagariToIast → TagExtractor → QuoteAssembler → Deduplicator → IdAssigner → JsonWriter → RoundTripValidator
    - Print processing summary to stdout on success (files processed, quotes found, duplicates removed, quotes written)
    - Exit with code 0 on success, non-zero on validation failure or errors
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 8.2, 8.3, 9.1, 9.2_

  - [ ]* 13.2 Write unit tests for CLI error handling
    - Test missing directory argument, non-existent directory, empty directory (no `.html` files)
    - Test successful pipeline execution with sample HTML files
    - _Requirements: 1.3, 1.4, 9.1, 9.2_

- [x] 14. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document (Properties 1–12)
- The extractor reuses existing `QuoteDto` and `QuoteFile` data models from the app module
- Kotest Property is already a project dependency; tests use `StringSpec` style consistent with existing tests
- All pipeline components are implemented as `object` singletons with pure functions for testability
