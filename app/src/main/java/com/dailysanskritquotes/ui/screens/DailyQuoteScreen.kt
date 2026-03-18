package com.dailysanskritquotes.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailysanskritquotes.ui.viewmodel.DailyQuoteUiState
import com.dailysanskritquotes.ui.viewmodel.DailyQuoteViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyQuoteScreen(
    viewModel: DailyQuoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val customTags by viewModel.customTags.collectAsState()
    val allTagNames by viewModel.allTagNames.collectAsState()
    val scrollToEnd by viewModel.scrollToEnd.collectAsState()
    val context = LocalContext.current
    var showAddTagDialog by remember { mutableStateOf(false) }
    var revealButtonVisible by remember { mutableStateOf(false) }

    // Refresh quote each time the screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshIfNewDay()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is DailyQuoteUiState.Loading -> {
                CircularProgressIndicator()
            }
            is DailyQuoteUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is DailyQuoteUiState.Success -> {
                val history = state.quoteHistory
                val pagerState = rememberPagerState(
                    initialPage = state.currentPageIndex,
                    pageCount = { history.size }
                )

                // Scroll to end when a new quote is revealed
                LaunchedEffect(scrollToEnd) {
                    if (scrollToEnd) {
                        pagerState.animateScrollToPage(history.lastIndex)
                        viewModel.onScrollToEndConsumed()
                    }
                }

                // Sync pager page changes to ViewModel
                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { page ->
                        viewModel.onPageChanged(page)
                    }
                }

                // Triple-tap detection state
                var tapCount by remember { mutableIntStateOf(0) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        tapCount++
                                        if (tapCount >= 3) {
                                            revealButtonVisible = !revealButtonVisible
                                            tapCount = 0
                                        }
                                    }
                                )
                            }
                    ) { page ->
                        val item = history[page]
                        val pageTags = remember(item.quote.tags) {
                            try {
                                Json.decodeFromString<List<String>>(item.quote.tags)
                            } catch (_: Exception) {
                                emptyList()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            QuoteCardContent(
                                quote = item.quote,
                                shownDate = item.shownDate,
                                isFavorite = if (page == state.currentPageIndex) {
                                    // Use live favorite state for current page
                                    item.isFavorite
                                } else item.isFavorite,
                                customTags = if (page == state.currentPageIndex) customTags else emptyList(),
                                predefinedTags = pageTags,
                                onToggleFavorite = { viewModel.toggleFavorite() },
                                onShare = { viewModel.shareQuote(context) },
                                onAddTag = { showAddTagDialog = true },
                                onRemoveTag = { tagName -> viewModel.removeTag(tagName) }
                            )
                        }
                    }

                    // Reset tap count after timeout
                    LaunchedEffect(tapCount) {
                        if (tapCount > 0) {
                            delay(500)
                            tapCount = 0
                        }
                    }

                    // Hidden reveal button
                    if (revealButtonVisible) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.revealNextQuote() },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text("Reveal Next Quote")
                        }
                    }
                }

                // Add Tag Dialog
                if (showAddTagDialog) {
                    AddTagDialog(
                        onDismiss = { showAddTagDialog = false },
                        onConfirm = { tagName ->
                            viewModel.addTag(tagName)
                            showAddTagDialog = false
                        },
                        existingTagNames = allTagNames
                    )
                }
            }
        }
    }
}
