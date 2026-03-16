package com.dailysanskritquotes.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.domain.DailyQuoteSelector
import com.dailysanskritquotes.domain.FavoritesManager
import com.dailysanskritquotes.domain.ShareHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class DailyQuoteUiState {
    object Loading : DailyQuoteUiState()
    data class Success(val quote: QuoteEntity, val isFavorite: Boolean) : DailyQuoteUiState()
    data class Error(val message: String) : DailyQuoteUiState()
}

@HiltViewModel
class DailyQuoteViewModel @Inject constructor(
    private val dailyQuoteSelector: DailyQuoteSelector,
    private val favoritesManager: FavoritesManager,
    private val shareHandler: ShareHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyQuoteUiState>(DailyQuoteUiState.Loading)
    val uiState: StateFlow<DailyQuoteUiState> = _uiState.asStateFlow()

    init {
        loadTodayQuote()
    }

    private fun loadTodayQuote() {
        viewModelScope.launch {
            try {
                val quote = dailyQuoteSelector.getQuoteForDate(LocalDate.now())
                val isFavorite = favoritesManager.isFavorite(quote.id)
                _uiState.value = DailyQuoteUiState.Success(quote, isFavorite)
            } catch (e: Exception) {
                _uiState.value = DailyQuoteUiState.Error(e.message ?: "Failed to load quote")
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value
        if (current !is DailyQuoteUiState.Success) return
        viewModelScope.launch {
            favoritesManager.toggleFavorite(current.quote.id)
            val isFavorite = favoritesManager.isFavorite(current.quote.id)
            _uiState.value = current.copy(isFavorite = isFavorite)
        }
    }

    fun shareQuote(context: Context) {
        val current = _uiState.value
        if (current !is DailyQuoteUiState.Success) return
        val quote = current.quote
        shareHandler.share(context, quote.sanskritText, quote.englishTranslation, quote.attribution)
    }
}
