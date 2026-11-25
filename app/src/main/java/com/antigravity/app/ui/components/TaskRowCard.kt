package com.antigravity.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.antigravity.app.data.model.TaskItem
import com.antigravity.app.data.model.TaskPriority
import com.antigravity.app.ui.theme.AmberAccent
import com.antigravity.app.ui.theme.AmberMuted
import com.antigravity.app.ui.theme.CyanAccent
import com.antigravity.app.ui.theme.CyanMuted
import com.antigravity.app.ui.theme.EmeraldAccent
import com.antigravity.app.ui.theme.EmeraldMuted
import com.antigravity.app.ui.theme.GlassBorder
import com.antigravity.app.ui.theme.Navy950
import com.antigravity.app.ui.theme.RoseAccent
import com.antigravity.app.ui.theme.RoseMuted
import com.antigravity.app.ui.theme.TextMuted
import com.antigravity.app.ui.theme.TextPrimary
import com.antigravity.app.ui.theme.TextSecondary

@Composable
fun TaskRowCard(
    task: TaskItem,
    onToggleCompleted: () -> Unit,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.56f else 1f,
        animationSpec = tween(250),
        label = "task_alpha"
    )
    val checkBg by animateColorAsState(
        targetValue = if (task.isCompleted) EmeraldAccent else Color.Transparent,
        animationSpec = tween(220),
        label = "check_bg"
    )
    val checkBorder by animateColorAsState(
        targetValue = if (task.isCompleted) EmeraldAccent else CyanAccent.copy(alpha = 0.7f),
        animationSpec = tween(220),
        label = "check_border"
    )

    val (priorityColor, priorityBg) = when (task.priority) {
        TaskPriority.HIGH -> RoseAccent to RoseMuted
        TaskPriority.MEDIUM -> AmberAccent to AmberMuted
        TaskPriority.LOW -> EmeraldAccent to EmeraldMuted
    }
    val isOverdue = !task.isCompleted &&
        task.dueAtMillis != null &&
        task.dueAtMillis < System.currentTimeMillis()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderColor = if (task.isCompleted) GlassBorder.copy(alpha = 0.12f) else GlassBorder,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(checkBg)
                    .border(1.5.dp, checkBorder, CircleShape)
                    .clickable(onClick = onToggleCompleted),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Completed",
                        tint = Navy950,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (task.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    ),
                    color = if (task.isCompleted) TextMuted else TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TagPill(text = task.priority.label, textColor = priorityColor, bgColor = priorityBg)
                    TagPill(text = task.category.label, textColor = CyanAccent, bgColor = CyanMuted)

                    if (task.dueAtMillis != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isOverdue) RoseMuted else Color.White.copy(alpha = 0.06f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = if (isOverdue) RoseAccent else TextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = formatDueTimestamp(task.dueAtMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) RoseAccent else TextSecondary
                            )
                        }
                    }
                }
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete task",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TagPill(
    text: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}
