package com.sonda11.argus.remote

import android.content.Context
import com.sonda11.argus.remote.adapter.haptics.AndroidHapticsAdapter
import com.sonda11.argus.remote.adapter.hid.AndroidHidKeyboardAdapter
import com.sonda11.argus.remote.adapter.settings.DataStoreSettingsAdapter
import com.sonda11.argus.remote.adapter.time.ElapsedRealtimeClock
import com.sonda11.argus.remote.core.application.SendSlideCommand
import com.sonda11.argus.remote.core.domain.port.ClockPort
import com.sonda11.argus.remote.core.domain.port.HapticsPort
import com.sonda11.argus.remote.core.domain.port.KeyboardPort
import com.sonda11.argus.remote.core.domain.port.SettingsPort

/**
 * Composition root. Dependencies are wired once and injected through
 * constructors; no annotation processor is needed for this app size.
 */
class AppContainer(context: Context) {
    val clock: ClockPort = ElapsedRealtimeClock()
    val keyboard: KeyboardPort = AndroidHidKeyboardAdapter(context)
    val settings: SettingsPort = DataStoreSettingsAdapter(context)
    val haptics: HapticsPort = AndroidHapticsAdapter(context)
    val sendSlideCommand = SendSlideCommand(keyboard, haptics)
}
