package com.sonda11.argus.remote.adapter.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.sonda11.argus.remote.BuildConfig
import com.sonda11.argus.remote.R
import com.sonda11.argus.remote.adapter.ui.common.formatClock
import com.sonda11.argus.remote.adapter.ui.common.parseTargetSeconds
import com.sonda11.argus.remote.core.domain.model.RemoteSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: RemoteSettings,
    onDismiss: () -> Unit,
    onSaveTarget: (Long) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onKeepScreenOnChanged: (Boolean) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = stringResource(R.string.settings_target),
                style = MaterialTheme.typography.titleMedium,
            )

            var targetInput by remember(settings.target) {
                mutableStateOf(formatClock(settings.target))
            }
            val parsedTarget = parseTargetSeconds(targetInput)

            OutlinedTextField(
                value = targetInput,
                onValueChange = { targetInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.settings_target_hint)) },
                singleLine = true,
                isError = parsedTarget == null,
                supportingText = {
                    if (parsedTarget == null) {
                        Text(stringResource(R.string.settings_invalid_target))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = { parsedTarget?.let(onSaveTarget) },
                    enabled = parsedTarget != null,
                ) {
                    Text(stringResource(R.string.settings_save))
                }
            }

            HorizontalDivider()

            SettingToggleRow(
                title = stringResource(R.string.settings_haptics),
                checked = settings.hapticsEnabled,
                onCheckedChange = onHapticsChanged,
            )

            SettingToggleRow(
                title = stringResource(R.string.settings_keep_screen_on),
                checked = settings.keepScreenOn,
                onCheckedChange = onKeepScreenOnChanged,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
