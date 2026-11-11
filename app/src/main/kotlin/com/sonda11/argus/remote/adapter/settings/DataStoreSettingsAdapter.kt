package com.sonda11.argus.remote.adapter.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sonda11.argus.remote.core.domain.model.RemoteSettings
import com.sonda11.argus.remote.core.domain.port.SettingsPort
import java.io.IOException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.remoteSettingsDataStore by preferencesDataStore(name = "remote_settings")

/**
 * DataStore-backed implementation of [SettingsPort].
 */
class DataStoreSettingsAdapter(
    context: Context,
) : SettingsPort {

    private val appContext = context.applicationContext

    override val settings: Flow<RemoteSettings> = appContext.remoteSettingsDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            val targetSeconds = (preferences[Keys.TARGET_SECONDS] ?: DEFAULT_TARGET_SECONDS)
                .coerceIn(RemoteSettings.MIN_TARGET_SECONDS, RemoteSettings.MAX_TARGET_SECONDS)

            RemoteSettings(
                target = targetSeconds.seconds,
                hapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: true,
                keepScreenOn = preferences[Keys.KEEP_SCREEN_ON] ?: true,
            )
        }

    override suspend fun save(settings: RemoteSettings) {
        appContext.remoteSettingsDataStore.edit { preferences ->
            preferences[Keys.TARGET_SECONDS] = settings.target.inWholeSeconds.coerceIn(
                RemoteSettings.MIN_TARGET_SECONDS,
                RemoteSettings.MAX_TARGET_SECONDS,
            )
            preferences[Keys.HAPTICS_ENABLED] = settings.hapticsEnabled
            preferences[Keys.KEEP_SCREEN_ON] = settings.keepScreenOn
        }
    }

    private object Keys {
        val TARGET_SECONDS = longPreferencesKey("target_seconds")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    private companion object {
        const val DEFAULT_TARGET_SECONDS = 17L * 60L + 55L
    }
}
