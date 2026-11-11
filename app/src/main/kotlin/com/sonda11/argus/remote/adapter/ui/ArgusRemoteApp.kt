package com.sonda11.argus.remote.adapter.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonda11.argus.remote.AppContainer
import com.sonda11.argus.remote.R
import com.sonda11.argus.remote.adapter.ui.common.StatusChip
import com.sonda11.argus.remote.adapter.ui.common.failureText
import com.sonda11.argus.remote.adapter.ui.remote.RemoteScreen
import com.sonda11.argus.remote.adapter.ui.settings.SettingsSheet
import com.sonda11.argus.remote.adapter.ui.setup.SetupScreen

private enum class Screen {
    Setup,
    Remote,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArgusRemoteApp(container: AppContainer) {
    val viewModel: RemoteViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = RemoteViewModel(
                keyboard = container.keyboard,
                settingsPort = container.settings,
                clock = container.clock,
                sendSlideCommand = container.sendSlideCommand,
            ) as T
        },
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var screen by rememberSaveable { mutableStateOf(Screen.Setup) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val requiredPermissions = remember { requiredBluetoothPermissions() }
    var permissionsGranted by remember {
        mutableStateOf(hasAllPermissions(context, requiredPermissions))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        permissionsGranted = requiredPermissions.isEmpty() ||
            requiredPermissions.all { permission -> result[permission] == true }
        if (permissionsGranted) {
            viewModel.refresh()
        }
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refresh()
    }

    val discoverableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refresh()
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted && requiredPermissions.isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            viewModel.refresh()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        val granted = hasAllPermissions(context, requiredPermissions)
        if (granted != permissionsGranted) {
            permissionsGranted = granted
        }
        // The user may have paired from the computer while the app was in the
        // background, so the keyboard state and the bonded list are re-read.
        viewModel.refresh()
    }

    LaunchedEffect(error) {
        val failure = error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(failureText(context, failure))
        viewModel.clearError()
    }

    val view = LocalView.current
    DisposableEffect(state.settings.keepScreenOn) {
        view.keepScreenOn = state.settings.keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    StatusChip(
                        status = state.keyboard,
                        onClick = { screen = Screen.Setup },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .widthIn(max = 132.dp),
                    )
                    TextButton(onClick = { showSettings = true }) {
                        Text(stringResource(R.string.remote_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (screen) {
            Screen.Setup -> SetupScreen(
                state = state,
                permissionsGranted = permissionsGranted,
                onGrantPermissions = {
                    if (requiredPermissions.isNotEmpty()) {
                        permissionLauncher.launch(requiredPermissions)
                    }
                },
                onOpenAppSettings = { openAppSettings(context) },
                onEnableBluetooth = { launchEnableBluetooth(enableBluetoothLauncher) },
                onEnableKeyboard = viewModel::register,
                onMakeDiscoverable = { launchDiscoverable(discoverableLauncher) },
                onRefresh = viewModel::refresh,
                onConnect = viewModel::connect,
                onOpenRemote = { screen = Screen.Remote },
                modifier = Modifier.padding(padding),
            )

            Screen.Remote -> RemoteScreen(
                state = state,
                onCommand = viewModel::send,
                onStartTimer = viewModel::startTimer,
                onPauseTimer = viewModel::pauseTimer,
                onResetTimer = viewModel::resetTimer,
                onOpenConnection = { screen = Screen.Setup },
                onOpenSettings = { showSettings = true },
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showSettings) {
        SettingsSheet(
            settings = state.settings,
            onDismiss = { showSettings = false },
            onSaveTarget = viewModel::setTargetSeconds,
            onHapticsChanged = viewModel::setHapticsEnabled,
            onKeepScreenOnChanged = viewModel::setKeepScreenOn,
        )
    }
}

private fun requiredBluetoothPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
        )
    } else {
        emptyArray()
    }

private fun hasAllPermissions(context: Context, permissions: Array<String>): Boolean =
    permissions.all { permission ->
        ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun launchEnableBluetooth(launcher: ActivityResultLauncher<Intent>) {
    runCatching {
        launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }
}

private fun launchDiscoverable(launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
    }
    runCatching { launcher.launch(intent) }
}
