package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import android.graphics.Color
import android.view.ViewGroup
import android.content.ActivityNotFoundException
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var command: TextView
    private var voiceEngine: JarvisVoiceEngine? = null
    private var voiceReady = false
    private val requestAudio = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(3, 10, 22)
        window.navigationBarColor = Color.rgb(3, 10, 22)
        voiceEngine = JarvisVoiceEngine(this) { ready ->
            runOnUiThread {
                voiceReady = ready
                if (!ready) status.text = "HINDI VOICE NOT INSTALLED"
            }
        }
        buildScreen()
    }

    private fun speakHindi(message: String) {
        if (voiceReady) voiceEngine?.speakHindi(message)
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 32)
            setBackgroundColor(Color.rgb(3, 10, 22))
        }
        val title = TextView(this).apply {
            text = "I'M READY"; textSize = 30f; letterSpacing = 0.18f
            gravity = Gravity.CENTER; setTextColor(Color.rgb(103, 232, 249))
        }
        val name = TextView(this).apply {
            text = "J.A.R.V.I.S."; textSize = 22f; letterSpacing = 0.22f
            gravity = Gravity.CENTER; setTextColor(Color.WHITE)
        }
        status = TextView(this).apply {
            text = "SYSTEM READY"; textSize = 16f; gravity = Gravity.CENTER
            setTextColor(Color.rgb(160, 205, 224))
        }
        command = TextView(this).apply {
            text = "माइक दबाकर हिंदी में बात करें।"; textSize = 17f
            gravity = Gravity.CENTER; setTextColor(Color.rgb(190, 216, 231))
            setPadding(12, 28, 12, 28)
        }
        val mic = Button(this).apply {
            text = "🎙  JARVIS से बात करें"; textSize = 16f
            setOnClickListener { requestVoiceCommand() }
        }
        listOf(title, name, status, command, mic).forEach {
            root.addView(it, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        setContentView(root)
    }

    private fun requestVoiceCommand() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio); return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "VOICE SERVICE UNAVAILABLE"
            speakHindi("फोन में वॉइस रिकग्निशन सेवा उपलब्ध नहीं है।")
            Toast.makeText(this, "फोन में speech recognition service उपलब्ध नहीं है।", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS को कमांड दें")
        }
        try {
            status.text = "LISTENING..."
            startActivityForResult(intent, 1002)
        } catch (_: ActivityNotFoundException) {
            status.text = "VOICE SERVICE UNAVAILABLE"
            speakHindi("वॉइस सेवा उपलब्ध नहीं है।")
        }
    }

    @Deprecated("Activity result callback retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 1002 || resultCode != RESULT_OK) return
        val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        command.text = spoken.ifBlank { "कमांड समझ नहीं आई।" }
        handleCommand(spoken.lowercase(Locale.ROOT))
    }

    private fun handleCommand(spoken: String) {
        val apps = listOf(
            listOf("whatsapp", "व्हाट्सएप", "व्हाट्स ऐप") to ("WhatsApp" to "com.whatsapp"),
            listOf("instagram", "इंस्टाग्राम") to ("Instagram" to "com.instagram.android"),
            listOf("youtube", "यूट्यूब") to ("YouTube" to "com.google.android.youtube")
        )
        val match = apps.firstOrNull { entry -> entry.first.any { spoken.contains(it) } }?.second
        if (match != null) {
            val launch = packageManager.getLaunchIntentForPackage(match.second)
            if (launch != null) {
                startActivity(launch); status.text = "OPENING ${match.first.uppercase(Locale.ROOT)}"
                speakHindi("${match.first} खोल रहा हूँ।")
            } else {
                status.text = "${match.first.uppercase(Locale.ROOT)} NOT INSTALLED"
                speakHindi("${match.first} इस फोन में इंस्टॉल नहीं है।")
            }
        } else {
            status.text = "COMMAND RECEIVED — MORE FEATURES PENDING"
            speakHindi("आपकी कमांड मिली। यह सुविधा अभी तैयार की जा रही है।")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestAudio && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            requestVoiceCommand()
        } else if (requestCode == requestAudio) {
            status.text = "MICROPHONE PERMISSION REQUIRED"
            speakHindi("मुझे काम करने के लिए माइक्रोफोन की अनुमति चाहिए।")
        }
    }

    override fun onDestroy() {
        voiceEngine?.release()
        voiceEngine = null
        super.onDestroy()
    }
}
