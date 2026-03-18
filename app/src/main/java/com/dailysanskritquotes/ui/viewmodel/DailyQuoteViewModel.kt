package com.dailysanskritquotes.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailysanskritquotes.data.QuoteRepository
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

data class ShownQuoteItem(
    val quote: QuoteEntity,
    val shownDate: String,
    val isFavorite: Boolean,
    val customTags: List<CustomTagEntity>
)

sealed class DailyQuoteUiState {
    object Loading : DailyQuoteUiState()
    data class Success(
        val quoteHistory: List<ShownQuoteItem>,
        val currentPageIndex: Int
    ) : DailyQuoteUiState()
    data class Error(val message: String) : DailyQuoteUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyQuoteViewModel @Inject constructor(
    private val dailyQuoteSelector: DailyQuoteSelector,
    private val favoritesManager: FavoritesManager,
    private val shareHandler: ShareHandler,
    private val customTagManager: CustomTagManager,
    private val repository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyQuoteUiState>(DailyQuoteUiState.Loading)
    val uiState: StateFlow<DailyQuoteUiState> = _uiState.asStateFlow()

    /** Custom tags for the currently displayed quote, derived from current page. */
    val customTags: StateFlow<List<CustomTagEntity>> = _uiState
        .map { state ->
            val success = state as? DailyQuoteUiState.Success ?: return@map null
            success.quoteHistory.getOrNull(success.currentPageIndex)?.quote?.id
        }
        .flatMapLatest { quoteId ->
            if (quoteId != null) customTagManager.getTagsForQuote(quoteId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All distinct custom tag names for autocomplete suggestions. */
    val allTagNames: StateFlow<List<String>> = customTagManager.getAllTagNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Tracks whether we need to scroll to end after loading (e.g. after reveal). */
    private val _scrollToEnd = MutableStateFlow(false)
    val scrollToEnd: StateFlow<Boolean> = _scrollToEnd.asStateFlow()

    private var lastLoadedDate: LocalDate? = null

    init {
        loadQuoteHistory()
    }

    fun refreshIfNewDay() {
        val today = LocalDate.now()
        if (today != lastLoadedDate) {
            loadQuoteHistory()
        }
    }

    fun onPageChanged(index: Int) {
        val current = _uiState.value as? DailyQuoteUiState.Success ?: return
        if (index in current.quoteHistory.indices) {
            _uiState.value = current.copy(currentPageIndex = index)
        }
    }

    fun revealNextQuote() {
        viewModelScope.launch {
            try {
                dailyQuoteSelector.selectNextUnshownQuote()
                _scrollToEnd.value = true
                loadQuoteHistory(scrollToEnd = true)
            } catch (e: Exception) {
                // Silently fail — this is a dev-only feature
            }
        }
    }

    fun onScrollToEndConsumed() {
        _scrollToEnd.value = false
    }

    private fun loadQuoteHistory(scrollToEnd: Boolean = false) {
        val today = LocalDate.now()
        viewModelScope.launch {
            try {
                // Ensure today's quote exists
                dailyQuoteSelector.getQuoteForDate(today)

                // Fetch full history
                val history = repository.getShownQuoteHistory()
                val items = history.map { (quote, shownDate) ->
                    ShownQuoteItem(
                        quote = quote,
                        shownDate = shownDate,
                        isFavorite = favoritesManager.isFavorite(quote.id),
                        customTags = emptyList() // Tags loaded reactively via customTags flow
                    )
                }

                if (items.isEmpty()) {
                    _uiState.value = DailyQuoteUiState.Error("No quotes available")
                    return@launch
                }

                val pageIndex = if (scrollToEnd) items.lastIndex else items.lastIndex
                _uiState.value = DailyQuoteUiState.Success(
                    quoteHistory = items,
                    currentPageIndex = pageIndex
                )
                lastLoadedDate = today
            } catch (e: Exception) {
                _uiState.value = DailyQuoteUiState.Error(e.message ?: "Failed to load quotes")
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value as? DailyQuoteUiState.Success ?: return
        val item = current.quoteHistory.getOrNull(current.currentPageIndex) ?: return
        viewModelScope.launch {
            favoritesManager.toggleFavorite(item.quote.id)
            val isFavorite = favoritesManager.isFavorite(item.quote.id)
            val updatedHistory = current.quoteHistory.toMutableList()
            updatedHistory[current.currentPageIndex] = item.copy(isFavorite = isFavorite)
            _uiState.value = current.copy(quoteHistory = updatedHistory)
        }
    }

    fun shareQuote(context: Context) {
        val current = _uiState.value as? DailyQuoteUiState.Success ?: return
        val item = current.quoteHistory.getOrNull(current.currentPageIndex) ?: return
        val quote = item.quote
        shareHandler.share(context, quote.sanskritText, quote.englishTranslation, quote.attribution)
    }

    fun addTag(tagName: String) {
        val current = _uiState.value as? DailyQuoteUiState.Success ?: return
        val item = current.quoteHistory.getOrNull(current.currentPageIndex) ?: return
        viewModelScope.launch {
            customTagManager.addTag(item.quote.id, tagName)
        }
    }

    fun removeTag(tagName: String) {
        val current = _uiState.value as? DailyQuoteUiState.Success ?: return
        val item = current.quoteHistory.getOrNull(current.currentPageIndex) ?: return
        viewModelScope.launch {
            customTagManager.removeTag(item.quote.id, tagName)
        }
    }
}
