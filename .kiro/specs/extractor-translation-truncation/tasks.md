# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - English Translation Truncation by stripHkPrefix and isHarvardKyoto
  - **CRITICAL**: This test MUST FAIL on unfixed code — failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior — it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists in `stripHkPrefix` and `isHarvardKyoto`
  - **Scoped PBT Approach**: Scope the property to concrete failing cases from the bug condition
  - Create test file `extractor/src/test/kotlin/com/dailysanskritquotes/extractor/BugConditionExplorationTest.kt`
  - Use Kotest property-based testing (`io.kotest.property`)
  - Test 1 — `stripHkPrefix` truncation: Call `stripHkPrefix("Penance is the best way to live in the Kruta Yuga.")` and assert the full string is returned unchanged. On unfixed code this will return `"Kruta Yuga."` (truncated)
  - Test 2 — `stripHkPrefix` multi-sentence: Call `stripHkPrefix("The wise seek truth. Knowledge leads to liberation.")` and assert full string returned. On unfixed code this will return `"Knowledge leads to liberation."` (truncated)
  - Test 3 — `isHarvardKyoto` false positive: Call `isHarvardKyoto("He went to McDonald's for lunch")` and assert `false`. On unfixed code this returns `true` (1 incidental `cD` match triggers threshold)
  - Test 4 — `stripHkPrefix` proper noun: Call `stripHkPrefix("This is about Dharma and its meaning")` and assert full string returned unchanged
  - Property-based: For all generated English sentences with 0 HK-pattern words (`[a-z][A-Z]` mid-word transitions), assert `stripHkPrefix(input) == input` and `isHarvardKyoto(input) == false`
  - Note: `isHarvardKyoto` and `stripHkPrefix` are `private` — use reflection or make them `internal` for testing
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Tests FAIL (this is correct — it proves the bug exists)
  - Document counterexamples found (e.g., `stripHkPrefix("Penance is...Kruta Yuga.")` returns `"Kruta Yuga."`)
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Genuine HK Detection and Stripping Preserved
  - **IMPORTANT**: Follow observation-first methodology
  - Create test file `extractor/src/test/kotlin/com/dailysanskritquotes/extractor/PreservationPropertyTest.kt`
  - Use Kotest property-based testing (`io.kotest.property`)
  - Observe on UNFIXED code first, then encode observed behavior:
  - Observe: `isHarvardKyoto("nItidoShAH mRutyuH prIyate")` returns `true` on unfixed code
  - Observe: `isHarvardKyoto("dharmaH artha kAma mokShaH")` returns `true` on unfixed code
  - Observe: `stripHkPrefix("nItidoShAH mRutyuH The wise seek truth")` returns `"The wise seek truth"` on unfixed code
  - Observe: Lines containing dandas (`।`, `॥`) are classified as HK on unfixed code
  - Observe: Single-sentence English lines with no ambiguous patterns pass through unchanged
  - Property-based test 1: For all generated strings with 3+ words containing `[a-z][A-Z]` transitions (genuine HK), assert `isHarvardKyoto(input) == true`
  - Property-based test 2: For all generated mixed lines (HK words followed by English sentence), assert `stripHkPrefix` strips the HK prefix and returns the English portion
  - Property-based test 3: For all generated lines containing dandas (`।` or `॥`), assert `isHarvardKyoto(input) == true`
  - Unit test: Verify single-sentence English lines with no HK patterns pass through `stripHkPrefix` unchanged (this already works on unfixed code)
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 3. Fix for English translation truncation in stripHkPrefix and isHarvardKyoto

  - [x] 3.1 Implement the fix in QuoteBlockExtractor.kt
    - File: `extractor/src/main/kotlin/com/dailysanskritquotes/extractor/QuoteBlockExtractor.kt`
    - Change `isHarvardKyoto`: raise threshold from `hkMatches >= 1` to `hkMatches >= 3` — genuine HK lines have many `[a-z][A-Z]` transitions; normal English rarely exceeds 1-2
    - Keep the danda check `Regex("[।॥]").containsMatchIn(text)` unchanged
    - Change `stripHkPrefix`: remove the `prefix.all { it.isLetter() || it.isWhitespace() || it in ".,;:!?()" }` fallback branch — only strip when `Regex("[a-z][A-Z]").containsMatchIn(prefix)` is true
    - Keep the transition-point logic for finding the first English sentence start word unchanged
    - _Bug_Condition: isBugCondition(input) where input has 0 HK-pattern words but contains mid-line capitalized words, or has ≤1 HK match triggering isHarvardKyoto_
    - _Expected_Behavior: stripHkPrefix returns input unchanged for non-HK text; isHarvardKyoto returns false for lines with <3 HK matches_
    - _Preservation: Genuine HK lines (3+ transitions) still filtered; genuine mixed lines still stripped; danda lines still filtered; IDs unchanged_
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - English Translation Truncation Fixed
    - **IMPORTANT**: Re-run the SAME test from task 1 — do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Genuine HK Detection and Stripping Still Works
    - **IMPORTANT**: Re-run the SAME tests from task 2 — do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 3.4 Re-run extractor and generate diff report
    - Run the full extractor pipeline on `extractor/data/input/feed2.atom`
    - Compare new `extractor/output/quotes-full.json` with `extractor/output/quotes-full.backup.json`
    - Generate a diff report listing each quote ID where `englishTranslation` changed, showing old vs new text
    - Verify no quote IDs were added, removed, or reordered — only `englishTranslation` values may differ
    - _Requirements: 3.5, 4.1, 4.2, 4.3_

- [x] 4. Checkpoint - Ensure all tests pass
  - Run full test suite: `cd extractor && ./gradlew test`
  - Ensure all tests pass, ask the user if questions arise
