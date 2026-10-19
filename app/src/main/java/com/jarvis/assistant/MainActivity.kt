package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import android.graphics.Color
import android.view.ViewGroup
import android.content.ActivityNotFoundException

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var command: TextView
    private val requestAudio = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(3, 10, 22)
        window.navigationBarColor = Color.rgb(3, 10, 22)
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 32)
            setBackgroundColor(Color.rgb(3, 10, 22))
        }

        val title = TextView(this).apply {
            text = "I'M READY"
            textSize = 30f
            letterSpacing = 0.18f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(103, 232, 249))
        }
        val name = TextView(this).apply {
            text = "J.A.R.V.I.S."
            textSize = 22f
            letterSpacing = 0.22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        status = TextView(this).apply {
            text = "SYSTEM READY"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(160, 205, 224))
        }
        command = TextView(this).apply {
            text = "Tap the microphone and say a command."
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(190, 216, 231))
            setPadding(12, 28, 12, 28)
        }
        val mic = Button(this).apply {
            text = "🎙  START VOICE COMMAND"
            textSize = 16f
            setOnClickListener { requestVoiceCommand() }
        }
        root.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(name, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(status, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(command, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(mic, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(root)
    }

    private fun requestVoiceCommand() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio)
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "VOICE SERVICE UNAVAILABLE"
            Toast.makeText(this, "Is phone par speech recognition service available nahi hai.", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS ko command dein")
        }
        try {
            startActivityForResult(intent, 1002)
        } catch (_: ActivityNotFoundException) {
            status.text = "VOICE SERVICE UNAVAILABLE"
        }
    }

    @Deprecated("Activity result callback retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 1002 || resultCode != RESULT_OK) return
        val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        command.text = spoken.ifBlank { "Command samajh nahi aaya." }
        handleCommand(spoken.lowercase())
    }

    private fun handleCommand(spoken: String) {
        val apps = listOf(
            "whatsapp" to "com.whatsapp",
            "instagram" to "com.instagram.android",
            "youtube" to "com.google.android.youtube"
        )
        val match = apps.firstOrNull { spoken.contains(it.first) }
        if (match != null) {
            val launch = packageManager.getLaunchIntentForPackage(match.second)
            if (launch != null) {
                startActivity(launch)
                status.text = "OPENING ${match.first.uppercase()}"
            } else {
                status.text = "${match.first.uppercase()} NOT INSTALLED"
            }
        } else {
            status.text = "COMMAND RECEIVED — MORE FEATURES PENDING"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestAudio && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            requestVoiceCommand()
        } else if (requestCode == requestAudio) {
            status.text = "MICROPHONE PERMISSION REQUIRED"
        }
    }
}
