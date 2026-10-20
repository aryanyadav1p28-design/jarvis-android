package com.jarvis.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Context
import android.os.Bundle

/**
 * User-started foreground wake-word listener.
 * Uses the device's Android speech-recognition provider; it is not a local
 * neural wake-word engine and availability/battery use depend on the device.
 */
class JarvisWakeWordService : Service() {
    private var recognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var listening = false
    private var triggered = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            getSharedPreferences("jarvis_control", MODE_PRIVATE)
                .edit().putBoolean("screen_reading_active", false).apply()
            JarvisScreenReaderService.clearLatestScreenText()
            handler.removeCallbacksAndMessages(null)
            recognizer?.cancel()
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification("Hello JARVIS bolkar wake-word sun raha hoon"), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification("Hello JARVIS bolkar wake-word sun raha hoon"))
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            updateNotification("Is phone par speech recognition available nahi hai")
            stopSelf()
            return START_NOT_STICKY
        }
        triggered = false
        startListening()
        return START_NOT_STICKY
    }

    private fun startListening() {
        if (triggered) return
        try {
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { listening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { listening = false }
                override fun onError(error: Int) {
                    listening = false
                    scheduleRetry()
                }
                override fun onResults(results: Bundle?) {
                    listening = false
                    val phrases = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                    val matched = phrases.any {
                        val normalized = it.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()
                        normalized.contains("hello jarvis") ||
                            normalized.contains("hey jarvis") ||
                            normalized.contains("हेलो जार्विस") ||
                            normalized.contains("हैलो जार्विस") ||
                            normalized.contains("हे जार्विस")
                    }
                    if (matched) wakeDetected() else scheduleRetry()
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val phrases = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                    if (phrases.any { it.lowercase().contains("hello jarvis") || it.lowercase().contains("हेलो जार्विस") }) wakeDetected()
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            }
            recognizer?.startListening(intent)
        } catch (_: Exception) {
            scheduleRetry()
        }
    }

    private fun scheduleRetry() {
        if (!triggered) handler.postDelayed({ startListening() }, 900)
    }

    private fun wakeDetected() {
        if (triggered) return
        triggered = true
        handler.removeCallbacksAndMessages(null)
        recognizer?.cancel()
        updateNotification("JARVIS activate ho gaya — command bolne ke liye notification tap karo")
        val open = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_WAKE_DETECTED, true)
        }
        val pending = PendingIntent.getActivity(this, 201, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification("Hello JARVIS sun liya — tap karke command bolo", pending))
    }

    private fun notification(message: String, pendingIntent: PendingIntent? = null): Notification {
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL_ID) else Notification.Builder(this)
        builder.setContentTitle("JARVIS Wake Word")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(!triggered)

        // Stop only wake-word/microphone listening; keep Google Drive authorization untouched.
        val stopIntent = Intent(this, JarvisWakeWordService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 202, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(android.R.drawable.ic_media_pause, "JARVIS OFF", stopPending)

        if (pendingIntent != null) builder.setContentIntent(pendingIntent).setAutoCancel(false)
        return builder.build()
    }

    private fun updateNotification(message: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification(message))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "JARVIS Wake Word", NotificationManager.IMPORTANCE_LOW))
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.jarvis.assistant.STOP_WAKE_LISTENER"
        const val EXTRA_WAKE_DETECTED = "com.jarvis.assistant.WAKE_DETECTED"
        private const val CHANNEL_ID = "jarvis_wake_word"
        private const val NOTIFICATION_ID = 7310
    }
}
