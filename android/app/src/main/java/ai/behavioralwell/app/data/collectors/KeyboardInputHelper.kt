package ai.behavioralwell.app.data.collectors

import android.content.Context
import android.util.Log

object KeyboardInputHelper {

    private var lastKeyTimeMs: Long = 0L

    fun onKeyTyped(context: Context, isBackspace: Boolean = false) {
        val now = System.currentTimeMillis()
        val dwellTimeMs = 120L // Standard keypress dwell time
        val interKeyPauseMs = if (lastKeyTimeMs > 0) (now - lastKeyTimeMs).coerceAtMost(3000L) else 250L
        lastKeyTimeMs = now

        try {
            CollectorRegistry.getKeyboardCollector(context).recordKeyPressTiming(
                dwellTimeMs = dwellTimeMs,
                interKeyPauseMs = interKeyPauseMs,
                isBackspace = isBackspace
            )
            Log.d("KeyboardInputHelper", "Recorded keypress: dwell=${dwellTimeMs}ms pause=${interKeyPauseMs}ms backspace=$isBackspace")
        } catch (e: Exception) {
            Log.e("KeyboardInputHelper", "Error recording keypress timing", e)
        }
    }
}
