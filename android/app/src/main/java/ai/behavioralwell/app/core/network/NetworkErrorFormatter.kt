package ai.behavioralwell.app.core.network

import java.net.ConnectException
import java.net.SocketTimeoutException

object NetworkErrorFormatter {

    fun formatError(e: Throwable): String {
        val msg = e.localizedMessage ?: e.message ?: "Unknown network error"
        if (e is ConnectException || msg.contains("127.0.0.1") || msg.contains("Failed to connect") || msg.contains("ECONNREFUSED")) {
            return "Backend connection unavailable (127.0.0.1:8000). Connect USB and run the BehavioralWell USB Bridge (scripts/start_behavioralwell_usb.ps1)."
        }
        if (e is SocketTimeoutException) {
            return "Backend connection timed out. Verify USB bridge status."
        }
        return "Network error: $msg"
    }
}
