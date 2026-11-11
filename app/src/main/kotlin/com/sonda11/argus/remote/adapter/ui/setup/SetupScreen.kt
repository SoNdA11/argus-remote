package com.sonda11.argus.remote.adapter.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonda11.argus.remote.R
import com.sonda11.argus.remote.adapter.ui.RemoteUiState
import com.sonda11.argus.remote.core.domain.model.Host

@Composable
fun SetupScreen(
    state: RemoteUiState,
    permissionsGranted: Boolean,
    onGrantPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onEnableBluetooth: () -> Unit,
    onEnableKeyboard: () -> Unit,
    onMakeDiscoverable: () -> Unit,
    onRefresh: () -> Unit,
    onConnect: (Host) -> Unit,
    onOpenRemote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.setup_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.busy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (!state.keyboard.supported) {
            SetupCard(title = stringResource(R.string.setup_unsupported)) {
                Text(
                    text = stringResource(R.string.setup_offline_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SetupCard(title = stringResource(R.string.setup_step_permission)) {
            Text(
                text = stringResource(R.string.setup_step_permission_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!permissionsGranted) {
                Text(
                    text = stringResource(R.string.setup_permission_denied),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onGrantPermissions) {
                        Text(stringResource(R.string.setup_grant_permission))
                    }
                    OutlinedButton(onClick = onOpenAppSettings) {
                        Text(stringResource(R.string.setup_open_settings))
                    }
                }
            }
        }

        SetupCard(title = stringResource(R.string.setup_step_bluetooth)) {
            if (state.keyboard.bluetoothEnabled) {
                Text(
                    text = stringResource(R.string.setup_bluetooth_on),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = stringResource(R.string.setup_bluetooth_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!permissionsGranted) {
                    Text(
                        text = stringResource(R.string.setup_bluetooth_permission_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = onEnableBluetooth,
                    enabled = permissionsGranted,
                ) {
                    Text(stringResource(R.string.setup_turn_on_bluetooth))
                }
            }
        }

        SetupCard(title = stringResource(R.string.setup_step_keyboard)) {
            Text(
                text = stringResource(R.string.setup_keyboard_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state.keyboard.registered) {
                Text(
                    text = stringResource(R.string.setup_keyboard_registered),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Button(
                    onClick = onMakeDiscoverable,
                    enabled = permissionsGranted && !state.busy,
                ) {
                    Text(stringResource(R.string.setup_make_discoverable))
                }
                Text(
                    text = stringResource(R.string.setup_discoverable_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Button(
                    onClick = onEnableKeyboard,
                    enabled = permissionsGranted &&
                        state.keyboard.bluetoothEnabled &&
                        state.keyboard.supported &&
                        !state.busy,
                ) {
                    Text(stringResource(R.string.setup_enable_keyboard))
                }
            }
        }

        SetupCard(title = stringResource(R.string.setup_step_pair)) {
            Text(
                text = stringResource(R.string.setup_pair_body),
                style = MaterialTheme.typography.bodyMedium,
            )

            if (state.hosts.isEmpty()) {
                Text(
                    text = stringResource(R.string.setup_hosts_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                state.hosts.forEach { host ->
                    HostRow(
                        host = host,
                        connected = state.keyboard.connectedHost?.address == host.address,
                        enabled = state.keyboard.registered && !state.busy,
                        onConnect = { onConnect(host) },
                    )
                }
            }

            TextButton(onClick = onRefresh) {
                Text(stringResource(R.string.setup_refresh))
            }
        }

        Text(
            text = stringResource(R.string.setup_offline_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = onOpenRemote,
            enabled = state.keyboard.registered,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.setup_open_remote))
        }
    }
}

@Composable
private fun SetupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
    }
}

@Composable
private fun HostRow(
    host: Host,
    connected: Boolean,
    enabled: Boolean,
    onConnect: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = host.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (connected) {
            Text(
                text = stringResource(R.string.setup_connected),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            TextButton(
                onClick = onConnect,
                enabled = enabled,
            ) {
                Text(stringResource(R.string.setup_connect))
            }
        }
    }
}
