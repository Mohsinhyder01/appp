package com.antigravity.app.ui.components

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatDurationMs(durationMs: Long): String {
    if (durationMs <= 0L) return "0:00"
    val totalSeconds = durationMs / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

fun formatDueTimestamp(dueAtMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = dueAtMillis }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dayFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    val sameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    if (sameDay) {
        return "Today • ${timeFormat.format(Date(dueAtMillis))}"
    }

    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val isTomorrow = tomorrow.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        tomorrow.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    if (isTomorrow) {
        return "Tomorrow • ${timeFormat.format(Date(dueAtMillis))}"
    }

    return "${dayFormat.format(Date(dueAtMillis))} • ${timeFormat.format(Date(dueAtMillis))}"
}
