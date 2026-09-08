/*
 * Copyright (c) 2025 HamGap.
 *
 * HamGap phone-number authentication client.
 *
 * Talks to the HamGap OTP server:
 *   POST /request-code  {"phone": "+93700xxxxxxx"}
 *   POST /verify        {"phone": "+93700xxxxxxx", "code": "123456"}
 *
 * For local testing the server runs in Termux on the same phone, so we talk to
 * 127.0.0.1. When the server is deployed on Render, replace SERVER_URL with
 * the https URL (e.g. "https://hamgap-otp.onrender.com").
 */
package io.element.android.features.login.impl.screens.phone

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

object HamGapPhoneAuth {
    // Local (Termux) server. Change to the Render https URL after deployment.
    const val SERVER_URL: String = "http://127.0.0.1:3000"

    val isServerConfigured: Boolean
        get() = SERVER_URL.isNotBlank()

    sealed class AuthResult {
        /** Success. [userId] is set when the server returned one (after verification). */
        data class Success(val userId: String? = null) : AuthResult()

        /**
         * Failure. [message] is the server's error key (e.g. "wrong code"),
         * or null when the server could not be reached at all.
         */
        data class Error(val message: String?) : AuthResult()
    }

    suspend fun requestCode(phoneNumber: String): AuthResult {
        val body = JSONObject().put("phone", phoneNumber)
        return post("/request-code", body)
    }

    suspend fun verifyCode(phoneNumber: String, code: String): AuthResult {
        val body = JSONObject()
            .put("phone", phoneNumber)
            .put("code", code)
        // On success the OTP server returns the HamGap user id.
        return post("/verify", body)
    }

    private suspend fun post(path: String, body: JSONObject): AuthResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(SERVER_URL + path).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..399) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val json = if (text.isBlank()) JSONObject() else JSONObject(text)
            if (status in 200..399 && json.optBoolean("ok")) {
                AuthResult.Success(userId = json.optString("user_id").takeIf { it.isNotBlank() })
            } else {
                AuthResult.Error(json.optString("error"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Error(null)
        } finally {
            connection?.disconnect()
        }
    }

    /** True when the failure was a connection problem (server down / no network). */
    fun isNetworkError(result: AuthResult): Boolean =
        result is AuthResult.Error && result.message == null

    @Suppress("unused")
    @Throws(IOException::class)
    private fun ping() {
        // Reserved for future health-check usage.
    }
}
