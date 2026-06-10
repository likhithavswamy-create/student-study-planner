package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.NoteUi
import com.example.ui.util.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val notesUi by viewModel.notesUi.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilterId by remember { mutableStateOf<Int?>(null) } // null = All

    var showAddDialog by remember { mutableStateOf(false) }

    // Combined notes filtering
    val filteredNotes = remember(notesUi, searchQuery, selectedSubjectFilterId) {
        notesUi.filter { noteUi ->
            val matchQuery = noteUi.note.title.contains(searchQuery, ignoreCase = true) ||
                    noteUi.note.content.contains(searchQuery, ignoreCase = true)
            val matchSubject = selectedSubjectFilterId == null || noteUi.note.subjectId == selectedSubjectFilterId
            matchQuery && matchSubject
        }.sortedByDescending { it.note.updatedAt }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Search Bar component
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Notes") },
                placeholder = { Text("Search by title or content...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("notes_search_bar")
            )

            // Horizontal scrolling subject chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                item {
                    FilterChip(
                        selected = selectedSubjectFilterId == null,
                        onClick = { selectedSubjectFilterId = null },
                        label = { Text("All Subjects") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Custom subjects chips
                items(subjects) { subject ->
                    val color = Color(android.graphics.Color.parseColor(subject.colorHex))
                    FilterChip(
                        selected = selectedSubjectFilterId == subject.id,
                        onClick = { selectedSubjectFilterId = subject.id },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(subject.name)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor = color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedSubjectFilterId == subject.id,
                            selectedBorderColor = color,
                            selectedBorderWidth = 1.5.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No notes available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results match \"$searchQuery\"." else "Capture major lecture facts, lists, or formulas inside notes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Adaptive dual-grid layout for notes card structure
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredNotes, key = { it.note.id }) { noteUi ->
                        NoteCardItem(
                            noteUi = noteUi,
                            onDelete = { viewModel.deleteNote(noteUi.note.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun NoteCardItem(
    noteUi: NoteUi,
    onDelete: () -> Unit
) {
    val subjectColor = noteUi.subject?.colorHex?.let {
        Color(android.graphics.Color.parseColor(it))
    } ?: MaterialTheme.colorScheme.primaryContainer

    val noteDateFormatted = DateTimeFormatter.formatDate(noteUi.note.updatedAt)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header tag with Subject Color outline or tag
                Box(
                    modifier = Modifier
                        .background(subjectColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = noteUi.subject?.name ?: "General",
                        style = MaterialTheme.typography.labelSmall,
                        color = subjectColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.widthIn(max = 80.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete note",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = noteUi.note.title.ifBlank { "Untitled Note" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = noteUi.note.content.ifBlank { "This note contains no text." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = noteDateFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    viewModel: StudyViewModel
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create Note",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Selector
                SubjectSelector(
                    selectedSubjectId = selectedSubjectId,
                    onSubjectSelected = { selectedSubjectId = it },
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    placeholder = { Text("e.g. Physics Formula Sheet") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    placeholder = { Text("Type facts, links, calculations or list structures...") },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addNote(
                                subjectId = selectedSubjectId,
                                title = title,
                                content = content
                            )
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_note_button"),
                        enabled = title.isNotBlank() || content.isNotBlank()
                    ) {
                        Text("Save Note")
                    }
                }
            }
        }
    }
}
