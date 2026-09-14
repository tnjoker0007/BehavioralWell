package ai.behavioralwell.app.core.network

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorFormatter {

    fun formatError(e: Throwable): String {
        if (e is HttpException) {
            val code = e.code()
            return when (code) {
                500 -> "Server error (HTTP 500 Internal Server Error). Check backend logs."
                502, 503, 504 -> "Server unavailable (HTTP $code). Gateway/Server error."
                401, 403 -> "Authentication failed (HTTP $code). Please re-login."
                404 -> "API Endpoint not found (HTTP 404)."
                else -> "HTTP Error $code: ${e.message()}"
            }
        }

        val msg = e.localizedMessage ?: e.message ?: "Unknown error"

        if (e is ConnectException || msg.contains("Connection refused") || msg.contains("ECONNREFUSED") || msg.contains("Failed to connect to /127.0.0.1")) {
            return "Backend connection unavailable (127.0.0.1:8000). Connect USB and run the BehavioralWell USB Bridge (scripts/start_behavioralwell_usb.ps1)."
        }
        if (e is SocketTimeoutException) {
            return "Backend connection timed out. Verify USB bridge status."
        }
        if (e is UnknownHostException) {
            return "Host resolution failed: ${e.message}"
        }

        return "Network error: $msg"
    }
}

