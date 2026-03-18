package com.dailysanskritquotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailysanskritquotes.data.db.CustomTagEntity
import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.domain.CustomTagManager
import com.dailysanskritquotes.domain.SearchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchEngine: SearchEngine,
    private val customTagManager: CustomTagManager
) : ViewModel() {

    val query = MutableStateFlow("")

    /** Index of the selected search result for pager view, null = list view. */
    val selectedResultIndex = MutableStateFlow<Int?>(null)

    fun selectResult(index: Int) { selectedResultIndex.value = index }
    fun clearSelection() { selectedResultIndex.value = null }

    val searchResults: StateFlow<List<QuoteEntity>> = query
        .debounce(300)
        .flatMapLatest { q ->
            selectedResultIndex.value = null // Close pager when query changes
            if (q.isBlank() || q.trim().length < 3) flowOf(emptyList())
            else searchEngine.search(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All distinct custom tag names for autocomplete suggestions. */
    val allTagNames: StateFlow<List<String>> = customTagManager.getAllTagNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
