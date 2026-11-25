package com.antigravity.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.TaskCategory
import com.antigravity.app.data.model.TaskItem
import com.antigravity.app.ui.components.GlassCard
import com.antigravity.app.ui.components.TaskEditorDialog
import com.antigravity.app.ui.components.TaskRowCard
import com.antigravity.app.ui.theme.CyanAccent
import com.antigravity.app.ui.theme.CyanMuted
import com.antigravity.app.ui.theme.EmeraldAccent
import com.antigravity.app.ui.theme.EmeraldMuted
import com.antigravity.app.ui.theme.GlassBorder
import com.antigravity.app.ui.theme.GlassBorderCyan
import com.antigravity.app.ui.theme.Navy950
import com.antigravity.app.ui.theme.TextMuted
import com.antigravity.app.ui.theme.TextPrimary
import com.antigravity.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    bottomInsetPadding: androidx.compose.ui.unit.Dp = 140.dp
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showEditor = true
                },
                containerColor = CyanAccent,
                contentColor = Navy950,
                modifier = Modifier.padding(bottom = bottomInsetPadding - 64.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add task")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = bottomInsetPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Smart To-Do", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            text = "Organize tasks & launch focus audio",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (state.tasks.any { it.isCompleted }) {
                        TextButton(onClick = { viewModel.clearCompleted() }) {
                            Text("Clear Done", color = TextSecondary)
                        }
                    }
                }
            }

            // Focus Mode Integration Bar
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = GlassBorderCyan,
                    elevated = true
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Focus Mode Soundscape (Offline Ready)",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanAccent
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FocusModePill(
                                label = "Lo-Fi Focus",
                                icon = Icons.Rounded.Headphones,
                                accentColor = CyanAccent,
                                bgColor = CyanMuted,
                                isActive = state.playbackState.isPlaying &&
                                    state.playbackState.currentTrack?.mode == AudioMode.SONG,
                                onClick = { viewModel.startFocusAudio(AudioMode.SONG) },
                                modifier = Modifier.weight(1f)
                            )
                            FocusModePill(
                                label = "Calming Ayat",
                                icon = Icons.Rounded.MenuBook,
                                accentColor = EmeraldAccent,
                                bgColor = EmeraldMuted,
                                isActive = state.playbackState.isPlaying &&
                                    state.playbackState.currentTrack?.mode == AudioMode.AYAT,
                                onClick = { viewModel.startFocusAudio(AudioMode.AYAT) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Search tasks...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Status & Category Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskStatusFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = state.statusFilter == filter,
                                onClick = { viewModel.onStatusFilterSelected(filter) },
                                label = { Text(filter.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanMuted,
                                    selectedLabelColor = CyanAccent
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskCategory.entries.forEach { category ->
                            FilterChip(
                                selected = state.selectedCategory == category,
                                onClick = { viewModel.onCategorySelected(category) },
                                label = { Text(category.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldMuted,
                                    selectedLabelColor = EmeraldAccent
                                )
                            )
                        }
                    }
                }
            }

            if (state.tasks.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("No matching tasks", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Tap + to create a task with priority, due time, and category.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(state.tasks, key = { it.id }) { task ->
                    TaskRowCard(
                        task = task,
                        onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                        onClick = {
                            editingTask = task
                            showEditor = true
                        },
                        onDelete = {
                            viewModel.deleteTask(task)
                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = "Deleted \"${task.title}\"",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (res == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreTask(task)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showEditor) {
        TaskEditorDialog(
            initialTask = editingTask,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                viewModel.saveTask(saved)
                showEditor = false
            }
        )
    }
}

@Composable
private fun FocusModePill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    bgColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) accentColor.copy(alpha = 0.25f) else bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) TextPrimary else accentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
