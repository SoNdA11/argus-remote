package com.sonda11.argus.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sonda11.argus.remote.adapter.ui.ArgusRemoteApp
import com.sonda11.argus.remote.adapter.ui.theme.ArgusRemoteTheme

class MainActivity : ComponentActivity() {
    private val container by lazy {
        (application as ArgusRemoteApplication).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArgusRemoteTheme {
                ArgusRemoteApp(container = container)
            }
        }
    }
}
