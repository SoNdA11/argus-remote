package com.sonda11.argus.remote.adapter.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.sonda11.argus.remote.core.domain.port.HapticsPort

/**
 * Short vibration used as confirmation for a key that was actually sent.
 */
class AndroidHapticsAdapter(
    context: Context,
) : HapticsPort {

    private val appContext = context.applicationContext

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Vibrator::class.java)
    }

    override fun tap() {
        val vibrator = vibrator ?: return
        if (!vibrator.hasVibrator()) return

        runCatching {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    HAPTIC_DURATION_MILLIS,
                    HAPTIC_AMPLITUDE,
                ),
            )
        }
    }

    private companion object {
        const val HAPTIC_DURATION_MILLIS = 16L
        const val HAPTIC_AMPLITUDE = 90
    }
}
