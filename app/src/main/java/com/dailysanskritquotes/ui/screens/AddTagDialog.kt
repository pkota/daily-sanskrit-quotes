package com.dailysanskritquotes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val MAX_TAG_LENGTH = 30

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    existingTagNames: List<String>
) {
    var tagText by remember { mutableStateOf("") }

    val filteredSuggestions = remember(tagText, existingTagNames) {
        if (tagText.isBlank()) {
            emptyList()
        } else {
            existingTagNames.filter {
                it.contains(tagText.trim(), ignoreCase = true)
            }
        }
    }

    val isConfirmEnabled = tagText.trim().isNotBlank() && tagText.trim().length <= MAX_TAG_LENGTH

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tag") },
        text = {
            Column {
                TextField(
                    value = tagText,
                    onValueChange = { newValue ->
                        if (newValue.length <= MAX_TAG_LENGTH) {
                            tagText = newValue
                        }
                    },
                    placeholder = { Text("Enter tag name") },
                    singleLine = true,
                    supportingText = {
                        Text("${tagText.length}/$MAX_TAG_LENGTH")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (filteredSuggestions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                    ) {
                        items(filteredSuggestions) { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { tagText = suggestion }
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(tagText.trim()) },
                enabled = isConfirmEnabled
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
