package com.dailysanskritquotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailysanskritquotes.data.db.CustomTagEntity
import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.domain.CustomTagManager
import com.dailysanskritquotes.domain.FavoritesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesManager: FavoritesManager,
    private val customTagManager: CustomTagManager
) : ViewModel() {

    val favorites: StateFlow<List<QuoteEntity>> = favoritesManager.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All distinct custom tag names for autocomplete suggestions. */
    val allTagNames: StateFlow<List<String>> = customTagManager.getAllTagNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(quoteId: String) {
        viewModelScope.launch {
            favoritesManager.toggleFavorite(quoteId)
        }
    }

    /** Returns a Flow of custom tags for a specific quote. */
    fun getTagsForQuote(quoteId: String): Flow<List<CustomTagEntity>> =
        customTagManager.getTagsForQuote(quoteId)

    /** Adds a custom tag to a quote. */
    fun addTag(quoteId: String, tagName: String) {
        viewModelScope.launch {
            customTagManager.addTag(quoteId, tagName)
        }
    }

    /** Removes a custom tag from a quote. */
    fun removeTag(quoteId: String, tagName: String) {
        viewModelScope.launch {
            customTagManager.removeTag(quoteId, tagName)
        }
    }
}
