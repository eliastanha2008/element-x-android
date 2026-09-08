/*
 * Copyright (c) 2025 HamGap.
 *
 * Placeholder for the HamGap phone-number authentication service.
 * The UI is ready — once the server is deployed, set SERVER_URL below
 * and implement the send/verify calls.
 */
package io.element.android.features.login.impl.screens.phone

object HamGapPhoneAuth {
    // TODO: set this to the HamGap phone-auth server base URL once it is deployed.
    // Example: "https://auth.hamgap.example.com"
    const val SERVER_URL: String = ""

    val isServerConfigured: Boolean
        get() = SERVER_URL.isNotBlank()
}
