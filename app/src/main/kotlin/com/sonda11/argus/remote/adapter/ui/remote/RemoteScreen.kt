package com.sonda11.argus.remote.adapter.ui.remote

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonda11.argus.remote.R
import com.sonda11.argus.remote.adapter.ui.RemoteUiState
import com.sonda11.argus.remote.adapter.ui.common.formatClock
import com.sonda11.argus.remote.adapter.ui.common.formatOverTarget
import com.sonda11.argus.remote.core.domain.model.SlideCommand
import kotlin.time.Duration

@Composable
fun RemoteScreen(
    state: RemoteUiState,
    onCommand: (SlideCommand) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onOpenConnection: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!state.keyboard.connected) {
            NotConnectedBanner(onOpenConnection = onOpenConnection)
        }

        TimerCard(
            state = state,
            onStartTimer = onStartTimer,
            onPauseTimer = onPauseTimer,
            onResetTimer = onResetTimer,
            onOpenSettings = onOpenSettings,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BigRemoteButton(
                label = stringResource(R.string.remote_previous),
                arrow = "←",
                hint = stringResource(R.string.remote_hold_hint),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = { onCommand(SlideCommand.PREVIOUS) },
                onLongClick = { onCommand(SlideCommand.PREVIOUS_SLIDE) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            BigRemoteButton(
                label = stringResource(R.string.remote_next),
                arrow = "→",
                hint = stringResource(R.string.remote_hold_hint),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onCommand(SlideCommand.NEXT) },
                onLongClick = { onCommand(SlideCommand.NEXT_SLIDE) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryAction(
                label = stringResource(R.string.remote_black_screen),
                keyHint = "B",
                onClick = { onCommand(SlideCommand.BLACK_SCREEN) },
                modifier = Modifier.weight(1f),
            )
            SecondaryAction(
                label = stringResource(R.string.remote_full_screen),
                keyHint = "F",
                onClick = { onCommand(SlideCommand.FULL_SCREEN) },
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryAction(
                label = stringResource(R.string.remote_overview),
                keyHint = "Esc",
                onClick = { onCommand(SlideCommand.OVERVIEW) },
                modifier = Modifier.weight(1f),
            )
            SecondaryAction(
                label = stringResource(R.string.remote_theme),
                keyHint = "T",
                onClick = { onCommand(SlideCommand.TOGGLE_THEME) },
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = { onCommand(SlideCommand.FIRST_SLIDE) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.remote_first_slide))
            }
            TextButton(
                onClick = { onCommand(SlideCommand.LAST_SLIDE) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.remote_last_slide))
            }
        }
    }
}

@Composable
private fun NotConnectedBanner(onOpenConnection: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.remote_not_connected),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(onClick = onOpenConnection) {
                Text(stringResource(R.string.remote_connection))
            }
        }
    }
}

@Composable
private fun TimerCard(
    state: RemoteUiState,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val timer = state.timer
    val fraction = if (timer.target > Duration.ZERO) {
        (timer.elapsed / timer.target).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.timer_elapsed),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatClock(timer.elapsed),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(
                            R.string.timer_target_value,
                            formatClock(timer.target),
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (timer.overTarget) {
                        Text(
                            text = stringResource(
                                R.string.timer_over,
                                formatOverTarget(timer.elapsed - timer.target),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text(
                            text = stringResource(
                                R.string.timer_remaining_value,
                                formatClock(timer.remaining),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (timer.overTarget) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = if (timer.running) onPauseTimer else onStartTimer,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        stringResource(
                            if (timer.running) R.string.timer_pause else R.string.timer_start,
                        ),
                    )
                }
                OutlinedButton(
                    onClick = onResetTimer,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.timer_reset))
                }
            }

            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.timer_edit))
            }
        }
    }
}

@Composable
private fun BigRemoteButton(
    label: String,
    arrow: String,
    hint: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = arrow,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SecondaryAction(
    label: String,
    keyHint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = keyHint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
