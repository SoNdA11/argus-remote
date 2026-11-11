package com.sonda11.argus.remote.core.domain.model

/**
 * A Bluetooth host (the computer that receives the keyboard reports).
 */
data class Host(
    val address: String,
    val name: String,
)
