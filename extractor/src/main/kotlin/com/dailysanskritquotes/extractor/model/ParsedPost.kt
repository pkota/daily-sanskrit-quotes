package com.dailysanskritquotes.extractor.model

data class ParsedPost(
    val title: String,
    val bodyElements: List<String>,
    val labels: List<String>
)
