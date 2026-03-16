package com.dailysanskritquotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.domain.FavoritesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesManager: FavoritesManager
) : ViewModel() {

    val favorites: StateFlow<List<QuoteEntity>> = favoritesManager.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(quoteId: String) {
        viewModelScope.launch {
            favoritesManager.toggleFavorite(quoteId)
        }
    }
}
