/*
 * Copyright (c) 2025 HamGap.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.FilledTextField
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * HamGap phone-number login screen.
 * Collects the user's phone number, then navigates to the OTP verification screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLoginView(
    onBackClick: () -> Unit,
    onSendCode: (e164PhoneNumber: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var countryCode by rememberSaveable { mutableStateOf("+93") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    val digits = phoneNumber.filter { it.isDigit() }
    val isValid = digits.length in 7..13 && countryCode.filter { it.isDigit() }.length in 1..3
    val fullNumber = countryCode + digits

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
                    text = stringResource(R.string.hamgap_phone_login_title),
                    style = ElementTheme.typography.fontHeadingXlBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.hamgap_phone_login_subtitle),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        },
        footer = {
            Column {
                if (!HamGapPhoneAuth.isServerConfigured) {
                    Text(
                        text = stringResource(R.string.hamgap_phone_server_notice),
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                    Spacer(Modifier.height(16.dp))
                }
                Button(
                    text = stringResource(R.string.hamgap_phone_send_code),
                    onClick = {
                        if (isValid) {
                            showError = false
                            onSendCode(fullNumber)
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTextField(
                value = countryCode,
                onValueChange = { value ->
                    countryCode = value.filter { it.isDigit() || it == '+' }.let { if (it.isEmpty() || it.first() == '+') it else "+$it" }
                },
                modifier = Modifier.fillMaxWidth(0.3f),
                label = { Text(stringResource(R.string.hamgap_phone_country_code_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
            )
            FilledTextField(
                value = phoneNumber,
                onValueChange = { value -> phoneNumber = value.filter { it.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.hamgap_phone_number_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = showError,
                supportingText = if (showError) {
                    { Text(stringResource(R.string.hamgap_phone_invalid_number)) }
                } else {
                    null
                },
            )
        }
    }
}
