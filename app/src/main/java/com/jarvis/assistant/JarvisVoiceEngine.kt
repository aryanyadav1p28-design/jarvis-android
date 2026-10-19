package com.jarvis.assistant

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Local voice output engine for JARVIS.
 * Uses Android's installed speech voice, prefers Hindi, and speaks at a
 * measured pace for clearer pronunciation. A cloud/custom voice provider can
 * be added behind this class later without changing the UI.
 */
class JarvisVoiceEngine(context: Context, private val onReady: (Boolean) -> Unit) :
    TextToSpeech.OnInitListener {

    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            onReady(false)
            return
        }

        val languageStatus = engine?.setLanguage(Locale("hi", "IN"))
        ready = languageStatus != TextToSpeech.LANG_MISSING_DATA &&
            languageStatus != TextToSpeech.LANG_NOT_SUPPORTED

        if (ready) {
            engine?.setSpeechRate(0.88f)
            engine?.setPitch(1.02f)
        }
        onReady(ready)
    }

    fun speakHindi(text: String) {
        if (ready && text.isNotBlank()) {
            engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis-hindi")
        }
    }

    fun stop() {
        engine?.stop()
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
    }
}
