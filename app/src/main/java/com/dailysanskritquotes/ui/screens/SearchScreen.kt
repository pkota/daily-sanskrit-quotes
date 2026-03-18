package com.dailysanskritquotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.ui.viewmodel.SearchViewModel
import kotlinx.serialization.json.Json

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allTagNames by viewModel.allTagNames.collectAsState()
    val selectedIndex by viewModel.selectedResultIndex.collectAsState()
    var addTagQuoteId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // List view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.query.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                placeholder = { Text("Search quotes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.query.value = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                query.isBlank() -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Search for quotes in Sanskrit or English",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(searchResults, key = { _, quote -> quote.id }) { index, quote ->
                            SearchResultCard(
                                quote = quote,
                                viewModel = viewModel,
                                onAddTagClick = { addTagQuoteId = quote.id },
                                onClick = { viewModel.selectResult(index) }
                            )
                        }
                    }
                }
            }
        }

        // Pager overlay
        if (selectedIndex != null && searchResults.isNotEmpty()) {
            SearchResultPager(
                results = searchResults,
                initialIndex = selectedIndex!!,
                viewModel = viewModel,
                onBack = { viewModel.clearSelection() }
            )
        }
    }

    // Add Tag Dialog
    if (addTagQuoteId != null) {
        AddTagDialog(
            onDismiss = { addTagQuoteId = null },
            onConfirm = { tagName ->
                viewModel.addTag(addTagQuoteId!!, tagName)
                addTagQuoteId = null
            },
            existingTagNames = allTagNames
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultPager(
    results: List<QuoteEntity>,
    initialIndex: Int,
    viewModel: SearchViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, results.lastIndex),
        pageCount = { results.size }
    )
    val allTagNames by viewModel.allTagNames.collectAsState()
    var addTagQuoteId by remember { mutableStateOf<String?>(null) }

    // Full-screen opaque overlay that covers everything including the search bar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to search results"
                    )
                }
                Text(
                    text = "${pagerState.currentPage + 1} / ${results.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Pager takes remaining space
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { page ->
                val quote = results[page]
                val customTags by viewModel.getTagsForQuote(quote.id).collectAsState(initial = emptyList())
                val predefinedTags = remember(quote.tags) {
                    try {
                        Json.decodeFromString<List<String>>(quote.tags)
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QuoteCardContent(
                        quote = quote,
                        shownDate = null,
                        isFavorite = quote.isFavorite,
                        customTags = customTags,
                        predefinedTags = predefinedTags,
                        onToggleFavorite = { },
                        onShare = { },
                        onAddTag = { addTagQuoteId = quote.id },
                        onRemoveTag = { tagName -> viewModel.removeTag(quote.id, tagName) }
                    )
                }
            }
        }
    }

    if (addTagQuoteId != null) {
        AddTagDialog(
            onDismiss = { addTagQuoteId = null },
            onConfirm = { tagName ->
                viewModel.addTag(addTagQuoteId!!, tagName)
                addTagQuoteId = null
            },
            existingTagNames = allTagNames
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchResultCard(
    quote: QuoteEntity,
    viewModel: SearchViewModel,
    onAddTagClick: () -> Unit,
    onClick: () -> Unit
) {
    val customTags by viewModel.getTagsForQuote(quote.id).collectAsState(initial = emptyList())

    val predefinedTags = remember(quote.tags) {
        try {
            Json.decodeFromString<List<String>>(quote.tags)
        } catch (_: Exception) {
            emptyList()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = quote.sanskritText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (quote.transliteration.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = quote.transliteration,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = quote.englishTranslation,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "— ${quote.attribution}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            if (predefinedTags.isNotEmpty() || customTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    predefinedTags.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                    customTags.forEach { customTag ->
                        AssistChip(
                            onClick = { viewModel.removeTag(quote.id, customTag.tagName) },
                            label = { Text(customTag.tagName) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove tag ${customTag.tagName}",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = onAddTagClick,
                label = { Text("Add Tag") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add tag",
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
