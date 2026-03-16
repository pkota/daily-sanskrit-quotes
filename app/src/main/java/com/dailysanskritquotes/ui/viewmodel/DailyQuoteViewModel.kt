package com.dailysanskritquotes.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailysanskritquotes.data.db.CustomTagEntity
import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.domain.CustomTagManager
import com.dailysanskritquotes.domain.DailyQuoteSelector
import com.dailysanskritquotes.domain.FavoritesManager
import com.dailysanskritquotes.domain.ShareHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class DailyQuoteUiState {
    object Loading : DailyQuoteUiState()
    data class Success(val quote: QuoteEntity, val isFavorite: Boolean) : DailyQuoteUiState()
    data class Error(val message: String) : DailyQuoteUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyQuoteViewModel @Inject constructor(
    private val dailyQuoteSelector: DailyQuoteSelector,
    private val favoritesManager: FavoritesManager,
    private val shareHandler: ShareHandler,
    private val customTagManager: CustomTagManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyQuoteUiState>(DailyQuoteUiState.Loading)
    val uiState: StateFlow<DailyQuoteUiState> = _uiState.asStateFlow()

    /** Custom tags for the currently displayed quote. */
    val customTags: StateFlow<List<CustomTagEntity>> = _uiState
        .map { state -> (state as? DailyQuoteUiState.Success)?.quote?.id }
        .flatMapLatest { quoteId ->
            if (quoteId != null) customTagManager.getTagsForQuote(quoteId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All distinct custom tag names for autocomplete suggestions. */
    val allTagNames: StateFlow<List<String>> = customTagManager.getAllTagNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    /** Adds a custom tag to the current quote. */
    fun addTag(tagName: String) {
        val current = _uiState.value
        if (current !is DailyQuoteUiState.Success) return
        viewModelScope.launch {
            customTagManager.addTag(current.quote.id, tagName)
        }
    }

    /** Removes a custom tag from the current quote. */
    fun removeTag(tagName: String) {
        val current = _uiState.value
        if (current !is DailyQuoteUiState.Success) return
        viewModelScope.launch {
            customTagManager.removeTag(current.quote.id, tagName)
        }
    }
}
