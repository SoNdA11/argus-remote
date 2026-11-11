package com.sonda11.argus.remote.adapter.ui.common

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonda11.argus.remote.R
import com.sonda11.argus.remote.core.domain.model.KeyboardFailure
import com.sonda11.argus.remote.core.domain.model.KeyboardStatus
import com.sonda11.argus.remote.core.domain.model.RemoteSettings
import java.util.Locale
import kotlin.time.Duration

@Composable
fun StatusChip(
    status: KeyboardStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when {
        !status.supported -> stringResource(R.string.status_unsupported)
        !status.bluetoothEnabled -> stringResource(R.string.status_bluetooth_off)
        status.connected -> stringResource(R.string.status_connected_short)
        status.registered -> stringResource(R.string.status_ready)
        else -> stringResource(R.string.status_off)
    }
    val dotColor = when {
        !status.supported || !status.bluetoothEnabled -> MaterialTheme.colorScheme.error
        status.connected -> Color(0xFF66E08A)
        status.registered -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

fun failureText(context: Context, failure: KeyboardFailure): String {
    val message = when (failure) {
        KeyboardFailure.Unsupported -> R.string.error_unsupported
        KeyboardFailure.BluetoothOff -> R.string.error_bluetooth_off
        KeyboardFailure.PermissionDenied -> R.string.error_permission_denied
        KeyboardFailure.NotRegistered -> R.string.error_not_registered
        KeyboardFailure.NotConnected -> R.string.error_not_connected
        KeyboardFailure.NoBondedHost -> R.string.error_no_bonded_host
        is KeyboardFailure.RegistrationFailed -> R.string.error_registration_failed
        is KeyboardFailure.ConnectionFailed -> R.string.error_connection_failed
        is KeyboardFailure.SendFailed -> R.string.error_send_failed
        is KeyboardFailure.Unknown -> R.string.error_unknown
    }
    val base = context.getString(message)
    val detail = when (failure) {
        is KeyboardFailure.RegistrationFailed -> failure.detail
        is KeyboardFailure.ConnectionFailed -> failure.detail
        is KeyboardFailure.SendFailed -> failure.detail
        is KeyboardFailure.Unknown -> failure.detail
        else -> null
    }
    return if (detail.isNullOrBlank()) base else "$base $detail"
}

fun formatClock(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds.coerceAtLeast(0L)
    return String.format(
        Locale.US,
        "%02d:%02d",
        totalSeconds / 60L,
        totalSeconds % 60L,
    )
}

fun formatOverTarget(duration: Duration): String = "+${formatClock(duration)}"

fun parseTargetSeconds(input: String): Long? {
    val parts = input.trim().split(":")
    if (parts.size != 2) return null

    val minutes = parts[0].toIntOrNull() ?: return null
    val seconds = parts[1].toIntOrNull() ?: return null
    if (minutes < 0 || seconds !in 0..59) return null

    val total = minutes * 60L + seconds
    return total.takeIf {
        it in RemoteSettings.MIN_TARGET_SECONDS..RemoteSettings.MAX_TARGET_SECONDS
    }
}
