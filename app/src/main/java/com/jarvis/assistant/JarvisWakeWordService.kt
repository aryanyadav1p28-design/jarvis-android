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
    private var awaitingCommand = false

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
        awaitingCommand = false
        startListening()
        return START_NOT_STICKY
    }

    private fun startListening() {
        if (triggered) return
        try {
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
            recognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    listening = true
                    updateNotification(if (awaitingCommand) "Wake word heard — listening for your command" else "Listening for Hello JARVIS")
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { listening = false }
                override fun onError(error: Int) {
                    listening = false
                    // Error 11 means the recognition service disconnected. Release this
                    // recognizer instance before retrying so Android can reconnect cleanly.
                    updateNotification(
                        if (error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED)
                            "Speech service disconnected (error 11) — reconnecting"
                        else "Speech recognition retrying (error $error)"
                    )
                    try { recognizer?.cancel() } catch (_: Exception) {}
                    try { recognizer?.destroy() } catch (_: Exception) {}
                    recognizer = null
                    handler.removeCallbacksAndMessages(null)
                    if (!triggered) handler.postDelayed({ startListening() }, 3000)
                }
                override fun onResults(results: Bundle?) {
                    listening = false
                    val phrases = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                    val wakePhrase = phrases.any { raw ->
                        val normalized = raw.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()
                        normalized.contains("hello jarvis") || normalized.contains("hey jarvis") ||
                            raw.lowercase().contains("हेलो जार्विस") ||
                            raw.lowercase().contains("हैलो जार्विस") ||
                            raw.lowercase().contains("हे जार्विस") ||
                            normalized.contains("jarvis")
                    }
                    val normalizedPhrases = phrases.map { it.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim() }
                    val voiceOff = normalizedPhrases.any {
                        it.contains("turn off") || it.contains("switch off") || it.contains("jarvis off") ||
                            it.endsWith(" off") || it.contains("stop screen reading")
                    } || phrases.any { it.lowercase().contains("जार्विस बंद") || it.lowercase().contains("स्क्रीन रीडिंग बंद") }
                    val voiceOn = normalizedPhrases.any {
                        it.contains("turn on") || it.contains("switch on") || it.contains("jarvis on") ||
                            it.endsWith(" on") || it.contains("start screen reading")
                    } || phrases.any { it.lowercase().contains("जार्विस चालू") || it.lowercase().contains("स्क्रीन रीडिंग चालू") }
                    val recognized = phrases.firstOrNull().orEmpty()
                    when {
                        voiceOff -> {
                            awaitingCommand = false
                            getSharedPreferences("jarvis_control", MODE_PRIVATE).edit()
                                .putBoolean("screen_reading_active", false).apply()
                            JarvisScreenReaderService.clearLatestScreenText()
                            updateNotification("Screen reading OFF — voice standby active; say Hello JARVIS turn on")
                            scheduleRetry()
                        }
                        voiceOn -> {
                            awaitingCommand = false
                            getSharedPreferences("jarvis_control", MODE_PRIVATE).edit()
                                .putBoolean("screen_reading_active", true).apply()
                            updateNotification("JARVIS screen reading ON — voice standby active")
                            scheduleRetry()
                        }
                        wakePhrase && normalizedPhrases.any { it.contains("open youtube") || it.contains("open whatsapp") || it.contains("open instagram") || it.contains("read screen") || it.contains("search ") } -> {
                            awaitingCommand = false
                            deliverCommand(recognized)
                        }
                        wakePhrase -> {
                            // Speech recognizers often split "Hello JARVIS" and the command
                            // into separate results. Keep listening for the next utterance.
                            awaitingCommand = true
                            updateNotification("Hello JARVIS heard — now say your command")
                            scheduleRetry()
                        }
                        awaitingCommand -> {
                            awaitingCommand = false
                            deliverCommand(recognized)
                        }
                        else -> scheduleRetry()
                    }
                }
                // Wait for final results so a partial "Hello JARVIS" does not
                // interrupt the rest of "Hello JARVIS, turn off/on".
                override fun onPartialResults(partialResults: Bundle?) {}
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
        // Give Android's speech service time to finish the previous session.
        if (!triggered) handler.postDelayed({ startListening() }, 1800)
    }

    private fun deliverCommand(command: String) {
        val normalized = command.lowercase()
            .replace(Regex("[^a-z0-9\\u0900-\\u097f ]"), " ")
            .replace(Regex("\\s+"), " ").trim()
        val appTargets = listOf(
            listOf("youtube", "यूट्यूब") to "com.google.android.youtube",
            listOf("whatsapp", "व्हाट्सएप", "व्हाट्स ऐप") to "com.whatsapp",
            listOf("instagram", "इंस्टाग्राम") to "com.instagram.android"
        )
        val target = appTargets.firstOrNull { entry -> entry.first.any { normalized.contains(it) } }?.second
        if (target != null) {
            val launch = packageManager.getLaunchIntentForPackage(target)
            if (launch != null) {
                try {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launch)
                    updateNotification("Command executed: ${command.take(65)}")
                    scheduleRetry()
                    return
                } catch (_: Exception) {
                    // Android may block opening an Activity from background; offer a notification action below.
                }
            }
        }

        // Non-app commands are handed to MainActivity. Android may require the user
        // to tap the notification when launching an Activity from the background.
        val open = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_COMMAND, command)
        }
        val pending = PendingIntent.getActivity(
            this, 203, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification("Command heard — tap to run: ${command.take(70)}", pending))
        scheduleRetry()
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
        const val EXTRA_COMMAND = "com.jarvis.assistant.COMMAND"
        private const val CHANNEL_ID = "jarvis_wake_word"
        private const val NOTIFICATION_ID = 7310
    }
}
