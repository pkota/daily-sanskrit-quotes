package com.dailysanskritquotes.extractor

object TagExtractor {

    private val KEYWORD_CATEGORIES = listOf(
        listOf("wisdom", "wise", "knowledge", "learn", "intellect", "intelligence") to "Wisdom",
        listOf("virtue", "virtuous", "noble", "righteous", "moral") to "Virtue",
        listOf("education", "teach", "student", "school", "study") to "Education",
        listOf("devotion", "devote", "god", "divine", "prayer", "worship", "lord", "krishna") to "Devotion",
        listOf("duty", "dharma", "responsibility", "obligation") to "Duty",
        listOf("friendship", "friend", "companion") to "Friendship",
        listOf("wealth", "money", "rich", "prosperity", "fortune") to "Wealth",
        listOf("courage", "brave", "valor", "hero", "strength") to "Courage",
        listOf("truth", "honest", "truthful", "sincer") to "Truth",
        listOf("patience", "calm", "peace", "serene", "tranquil") to "Peace",
        listOf("anger", "enemy", "hatred", "jealous", "greed") to "Self-Control",
        listOf("king", "ruler", "govern", "kingdom", "state") to "Governance",
        listOf("nature", "sun", "moon", "fire", "water", "earth") to "Nature",
        listOf("death", "life", "birth", "mortal", "immortal") to "Life",
        listOf("family", "wife", "husband", "child", "parent", "mother", "father", "son", "daughter") to "Family",
        listOf("speech", "word", "speak", "tongue", "silence", "silent") to "Speech"
    )

    fun fromLabels(labels: List<String>, attribution: String): List<String> {
        return labels
            .filter { !it.equals("Subhashita", ignoreCase = true) }
            .filter { !it.equals(attribution, ignoreCase = true) }
            .take(2)
    }

    fun fromTranslation(englishTranslation: String): List<String> {
        val lowerText = englishTranslation.lowercase()
        val matchedCategories = mutableListOf<Pair<String, Int>>()

        for ((keywords, category) in KEYWORD_CATEGORIES) {
            val matchCount = keywords.count { keyword -> lowerText.contains(keyword) }
            if (matchCount > 0) {
                matchedCategories.add(category to matchCount)
            }
        }

        return matchedCategories
            .sortedByDescending { it.second }
            .map { it.first }
            .take(2)
    }

    fun extract(labels: List<String>, attribution: String, englishTranslation: String): List<String> {
        val fromLabels = fromLabels(labels, attribution)
        return if (fromLabels.isNotEmpty()) {
            fromLabels
        } else {
            fromTranslation(englishTranslation)
        }
    }
}
