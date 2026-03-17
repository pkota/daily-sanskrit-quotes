package com.dailysanskritquotes.extractor

object DevanagariToIast {

    // Independent vowels
    private val VOWELS = mapOf(
        'अ' to "a", 'आ' to "ā", 'इ' to "i", 'ई' to "ī",
        'उ' to "u", 'ऊ' to "ū", 'ऋ' to "ṛ", 'ॠ' to "ṝ",
        'ऌ' to "ḷ", 'ॡ' to "ḹ", 'ए' to "e", 'ऐ' to "ai",
        'ओ' to "o", 'औ' to "au"
    )

    // Dependent vowel signs (matras)
    private val MATRAS = mapOf(
        'ा' to "ā", 'ि' to "i", 'ी' to "ī",
        'ु' to "u", 'ू' to "ū", 'ृ' to "ṛ", 'ॄ' to "ṝ",
        'ॢ' to "ḷ", 'ॣ' to "ḹ", 'े' to "e", 'ै' to "ai",
        'ो' to "o", 'ौ' to "au"
    )

    // Consonants (without implicit 'a')
    private val CONSONANTS = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ṅ",
        'च' to "c", 'छ' to "ch", 'ज' to "j", 'झ' to "jh", 'ञ' to "ñ",
        'ट' to "ṭ", 'ठ' to "ṭh", 'ड' to "ḍ", 'ढ' to "ḍh", 'ण' to "ṇ",
        'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m",
        'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v",
        'श' to "ś", 'ष' to "ṣ", 'स' to "s", 'ह' to "h"
    )

    // Devanagari numerals
    private val NUMERALS = mapOf(
        '०' to "0", '१' to "1", '२' to "2", '३' to "3", '४' to "4",
        '५' to "5", '६' to "6", '७' to "7", '८' to "8", '९' to "9"
    )

    private const val VIRAMA = '\u094D'      // ्
    private const val ANUSVARA = '\u0902'    // ं
    private const val VISARGA = '\u0903'     // ः
    private const val CHANDRABINDU = '\u0901' // ँ
    private const val NUKTA = '\u093C'       // ़
    private const val DANDA = '।'            // U+0964
    private const val DOUBLE_DANDA = '॥'     // U+0965

    // Nukta-modified consonant mappings (consonant + nukta combined)
    private val NUKTA_CONSONANTS = mapOf(
        'क' to "q",    // क़ → qa
        'ख' to "k͟h",  // ख़ → k͟ha
        'ग' to "ġ",    // ग़ → ġa
        'ज' to "z",    // ज़ → za
        'ड' to "ṛ",    // ड़ → ṛa
        'ढ' to "ṛh",   // ढ़ → ṛha
        'फ' to "f",    // फ़ → fa
        'य' to "ẏ"     // य़ → ẏa
    )

    fun transliterate(devanagari: String): String {
        val result = StringBuilder()
        var i = 0
        var pendingConsonant: String? = null  // consonant waiting for implicit 'a' resolution

        while (i < devanagari.length) {
            val ch = devanagari[i]

            // Look ahead for nukta following a consonant
            val hasNukta = ch in CONSONANTS &&
                i + 1 < devanagari.length &&
                devanagari[i + 1] == NUKTA

            when {
                // Consonant + Nukta combination
                hasNukta -> {
                    if (pendingConsonant != null) {
                        result.append("a")  // add implicit 'a' for previous consonant
                    }
                    val nuktaMapping = NUKTA_CONSONANTS[ch]
                    if (nuktaMapping != null) {
                        result.append(nuktaMapping)
                        pendingConsonant = nuktaMapping
                    } else {
                        // Fallback: use regular consonant mapping if no nukta variant
                        result.append(CONSONANTS[ch])
                        pendingConsonant = CONSONANTS[ch]
                    }
                    i += 2  // skip both consonant and nukta
                }

                // Virama - suppress implicit 'a' of pending consonant
                ch == VIRAMA -> {
                    pendingConsonant = null
                    i++
                }

                // Matra - replaces implicit 'a' of pending consonant
                ch in MATRAS -> {
                    pendingConsonant = null  // matra replaces implicit 'a'
                    result.append(MATRAS[ch])
                    i++
                }

                // Anusvara
                ch == ANUSVARA -> {
                    if (pendingConsonant != null) {
                        result.append("a")
                        pendingConsonant = null
                    }
                    result.append("ṃ")
                    i++
                }

                // Visarga
                ch == VISARGA -> {
                    if (pendingConsonant != null) {
                        result.append("a")
                        pendingConsonant = null
                    }
                    result.append("ḥ")
                    i++
                }

                // Chandrabindu
                ch == CHANDRABINDU -> {
                    if (pendingConsonant != null) {
                        result.append("a")
                        pendingConsonant = null
                    }
                    result.append("m̐")
                    i++
                }

                // Standalone Nukta (not preceded by consonant we handle above)
                ch == NUKTA -> {
                    i++
                }

                // Consonant (without nukta)
                ch in CONSONANTS -> {
                    if (pendingConsonant != null) {
                        result.append("a")  // add implicit 'a' for previous consonant
                    }
                    result.append(CONSONANTS[ch])
                    pendingConsonant = CONSONANTS[ch]
                    i++
                }

                // Independent vowel
                ch in VOWELS -> {
                    if (pendingConsonant != null) {
                        result.append("a")
                        pendingConsonant = null
                    }
                    result.append(VOWELS[ch])
                    i++
                }

                // Double danda (must check before single danda)
                ch == DOUBLE_DANDA -> {
                    if (pendingConsonant != null) {
                        result.append("a")
                        pendingConsonant = null
                    }
                    result.append("||")
                    i++
                }

                // Danda
                ch == DANDA -> {
                    if (pendingConsonant != null) {
                        result.append("a")
                        pendingConsonant = null
                    }
                    result.append("|")
                    i++
                }

                // Numeral
                ch in NUMERALS -> {
                    if (pendingConsonant != null) {
                        result.append("a")
                        pendingConsonant = null
                    }
                    result.append(NUMERALS[ch])
                    i++
                }

                // Non-Devanagari character - pass through
                else -> {
                    if (pendingConsonant != null) {
                        result.append("a")
                        pendingConsonant = null
                    }
                    result.append(ch)
                    i++
                }
            }
        }

        // Handle trailing consonant with implicit 'a'
        if (pendingConsonant != null) {
            result.append("a")
        }

        return result.toString()
    }
}
