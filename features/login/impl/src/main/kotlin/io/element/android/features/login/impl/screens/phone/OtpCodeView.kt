/*
 * Copyright (c) 2025 HamGap.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.phone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.FilledTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * HamGap OTP verification screen.
 * The user enters the 6-digit code received by SMS.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpCodeView(
    phoneNumber: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var code by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var resendCountdown by remember { mutableIntStateOf(60) }
    var resendTrigger by remember { mutableIntStateOf(0) }
    var isVerifying by rememberSaveable { mutableStateOf(false) }
    var isResending by rememberSaveable { mutableStateOf(false) }
    var verified by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Resolve error strings here (outside the non-composable onClick lambda),
    // then map server error keys to user-facing messages.
    val wrongLengthError = stringResource(R.string.hamgap_otp_wrong_length)
    val wrongCodeError = stringResource(R.string.hamgap_otp_wrong_code)
    val expiredError = stringResource(R.string.hamgap_otp_expired)
    val tooManyError = stringResource(R.string.hamgap_otp_too_many)
    val noCodeError = stringResource(R.string.hamgap_otp_request_new)
    val networkError = stringResource(R.string.hamgap_network_error)

    fun mapServerError(message: String?): String = when (message) {
        "wrong code" -> wrongCodeError
        "code expired, request a new one" -> expiredError
        "too many attempts" -> tooManyError
        "no code requested for this number" -> noCodeError
        else -> networkError
    }

    LaunchedEffect(resendTrigger) {
        resendCountdown = 60
        while (resendCountdown > 0) {
            delay(1_000)
            resendCountdown -= 1
        }
    }

    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                },
                title = {},
            )
        },
        header = {
            Column {
                Text(
                    text = stringResource(R.string.hamgap_otp_title),
                    style = ElementTheme.typography.fontHeadingXlBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.hamgap_otp_subtitle, phoneNumber),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        },
        footer = {
            Column {
                if (verified) {
                    Text(
                        text = stringResource(R.string.hamgap_otp_success),
                        style = ElementTheme.typography.fontBodyLgMedium,
                        color = ElementTheme.colors.textSuccessPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.hamgap_otp_success_next),
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                } else {
                    error?.let { message ->
                        Text(
                            text = message,
                            style = ElementTheme.typography.fontBodySmRegular,
                            color = ElementTheme.colors.textCriticalPrimary,
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    Button(
                        text = stringResource(R.string.hamgap_otp_verify),
                        onClick = {
                            if (code.length != 6) {
                                error = wrongLengthError
                            } else {
                                scope.launch {
                                    isVerifying = true
                                    error = null
                                    when (val result = HamGapPhoneAuth.verifyCode(phoneNumber, code)) {
                                        is HamGapPhoneAuth.AuthResult.Success -> verified = true
                                        is HamGapPhoneAuth.AuthResult.Error -> error = mapServerError(result.message)
                                    }
                                    isVerifying = false
                                }
                            }
                        },
                        showProgress = isVerifying,
                        enabled = !isVerifying,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    if (resendCountdown > 0) {
                        Text(
                            text = stringResource(R.string.hamgap_otp_resend_in, resendCountdown),
                            style = ElementTheme.typography.fontBodySmRegular,
                            color = ElementTheme.colors.textSecondary,
                        )
                    } else {
                        TextButton(
                            text = stringResource(R.string.hamgap_otp_resend),
                            showProgress = isResending,
                            enabled = !isResending,
                            onClick = {
                                scope.launch {
                                    isResending = true
                                    error = null
                                    when (HamGapPhoneAuth.requestCode(phoneNumber)) {
                                        is HamGapPhoneAuth.AuthResult.Success -> resendTrigger += 1
                                        is HamGapPhoneAuth.AuthResult.Error -> error = mapServerError(null)
                                    }
                                    isResending = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
    ) {
        FilledTextField(
            value = code,
            onValueChange = { value -> if (!verified) code = value.filter { it.isDigit() }.take(6) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.hamgap_otp_hint)) },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            enabled = !verified,
        )
    }
}
