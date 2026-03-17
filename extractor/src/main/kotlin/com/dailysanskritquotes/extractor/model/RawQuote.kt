package com.dailysanskritquotes.extractor.model

data class RawQuote(
    val sanskritText: String,
    val attribution: String,
    val englishTranslation: String,
    val labels: List<String>,
    val sourceFile: String
)
