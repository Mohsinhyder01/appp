package com.antigravity.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.antigravity.app.data.model.TaskCategory
import com.antigravity.app.data.model.TaskItem
import com.antigravity.app.data.model.TaskPriority
import com.antigravity.app.ui.theme.AmberAccent
import com.antigravity.app.ui.theme.CyanAccent
import com.antigravity.app.ui.theme.EmeraldAccent
import com.antigravity.app.ui.theme.GlassBorder
import com.antigravity.app.ui.theme.Navy900
import com.antigravity.app.ui.theme.Navy950
import com.antigravity.app.ui.theme.RoseAccent
import com.antigravity.app.ui.theme.Slate850
import com.antigravity.app.ui.theme.TextPrimary
import com.antigravity.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorDialog(
    initialTask: TaskItem?,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit
) {
    var title by remember(initialTask) { mutableStateOf(initialTask?.title ?: "") }
    var notes by remember(initialTask) { mutableStateOf(initialTask?.notes ?: "") }
    var priority by remember(initialTask) { mutableStateOf(initialTask?.priority ?: TaskPriority.MEDIUM) }
    var category by remember(initialTask) { mutableStateOf(initialTask?.category ?: TaskCategory.PERSONAL) }
    var dueAtMillis by remember(initialTask) { mutableStateOf(initialTask?.dueAtMillis) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Slate850,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) "New Task" else "Edit Task",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    placeholder = { Text("e.g., Memorize Surah Al-Mulk verses") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Text(text = "Priority", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { item ->
                        val chipColor = when (item) {
                            TaskPriority.HIGH -> RoseAccent
                            TaskPriority.MEDIUM -> AmberAccent
                            TaskPriority.LOW -> EmeraldAccent
                        }
                        FilterChip(
                            selected = priority == item,
                            onClick = { priority = item },
                            label = { Text(item.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.22f),
                                selectedLabelColor = chipColor
                            )
                        )
                    }
                }

                Text(text = "Category", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskCategory.entries.forEach { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.20f),
                                selectedLabelColor = CyanAccent
                            )
                        )
                    }
                }

                Text(text = "Schedule", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = CyanAccent)
                        Text(
                            text = " Date",
                            color = TextPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = EmeraldAccent)
                        Text(
                            text = " Time",
                            color = TextPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    if (dueAtMillis != null) {
                        TextButton(onClick = { dueAtMillis = null }) {
                            Text("Clear", color = RoseAccent)
                        }
                    }
                }

                if (dueAtMillis != null) {
                    Text(
                        text = "Due: ${dateFormatter.format(Date(dueAtMillis!!))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val trimmed = title.trim()
                        if (trimmed.isNotEmpty()) {
                            val updated = (initialTask ?: TaskItem(title = trimmed)).copy(
                                title = trimmed,
                                notes = notes.trim(),
                                priority = priority,
                                category = category,
                                dueAtMillis = dueAtMillis
                            )
                            onSave(updated)
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Navy950
                    )
                ) {
                    Text(
                        text = if (initialTask == null) "Create Task" else "Save Changes",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = dueAtMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { pickedDateUtc ->
                            val pickedCal = Calendar.getInstance().apply { timeInMillis = pickedDateUtc }
                            val currentCal = Calendar.getInstance().apply {
                                timeInMillis = dueAtMillis ?: System.currentTimeMillis()
                            }
                            currentCal.set(
                                pickedCal.get(Calendar.YEAR),
                                pickedCal.get(Calendar.MONTH),
                                pickedCal.get(Calendar.DAY_OF_MONTH)
                            )
                            dueAtMillis = currentCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) { Text("Confirm", color = CyanAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dueAtMillis ?: System.currentTimeMillis()
        }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Navy900,
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timeState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                                cal.set(Calendar.MINUTE, timeState.minute)
                                cal.set(Calendar.SECOND, 0)
                                dueAtMillis = cal.timeInMillis
                                showTimePicker = false
                            }
                        ) { Text("Set Time", color = CyanAccent) }
                    }
                }
            }
        }
    }
}
