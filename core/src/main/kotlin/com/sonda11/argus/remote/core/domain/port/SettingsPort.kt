package com.sonda11.argus.remote.core.domain.port

import com.sonda11.argus.remote.core.domain.model.RemoteSettings
import kotlinx.coroutines.flow.Flow

/**
 * Driven port for persisted user settings.
 */
interface SettingsPort {
    val settings: Flow<RemoteSettings>

    suspend fun save(settings: RemoteSettings)
}
