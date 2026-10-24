package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.graphics.Color
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var command: TextView
    private lateinit var endpointInput: EditText
    private lateinit var keyInput: EditText
    private lateinit var messageInput: EditText
    private val networkExecutor = Executors.newSingleThreadExecutor()
    private var voiceEngine: JarvisVoiceEngine? = null
    private var voiceReady = false
    private val requestAudio = 1001
    private val prefs by lazy { getSharedPreferences("jarvis_settings", MODE_PRIVATE) }

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
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 28, 24, 28)
            setBackgroundColor(Color.rgb(3, 10, 22))
        }
        fun label(text: String, size: Float = 16f) = TextView(this).apply {
            this.text = text
            textSize = size
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(190, 216, 231))
            setPadding(4, 10, 4, 8)
        }
        fun field(hintText: String, value: String = "") = EditText(this).apply {
            hint = hintText
            setText(value)
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(115, 145, 165))
            setPadding(14, 10, 14, 10)
            setBackgroundColor(Color.rgb(15, 28, 45))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 8
            }
        }

        root.addView(label("I'M READY", 30f))
        root.addView(label("J.A.R.V.I.S.", 22f))
        status = label("SYSTEM READY")
        root.addView(status)
        command = label("माइक दबाकर हिंदी में बात करें या नीचे संदेश लिखें।", 17f)
        root.addView(command)

        val mic = Button(this).apply {
            text = "🎙  JARVIS से बात करें"
            setOnClickListener { requestVoiceCommand() }
        }
        root.addView(mic, matchWidth())

        root.addView(label("CLOUD AI CONNECTION", 14f))
        endpointInput = field("Cloudflare Worker URL (https://…workers.dev)", prefs.getString("endpoint", "") ?: "")
        endpointInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        root.addView(endpointInput)
        keyInput = field("JARVIS client key", prefs.getString("client_key", "") ?: "")
        keyInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        root.addView(keyInput)

        messageInput = field("यहाँ JARVIS को संदेश लिखें…")
        messageInput.minLines = 2
        messageInput.maxLines = 4
        messageInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        root.addView(messageInput)
        val send = Button(this).apply {
            text = "☁  AI को भेजें"
            setOnClickListener {
                val message = messageInput.text.toString().trim()
                if (message.isNotEmpty()) askCloud(message)
            }
        }
        root.addView(send, matchWidth())
        root.addView(label("URL और key केवल अपने Worker के रखें। यह शुरुआती personal test connection है।", 12f))

        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = 8 }

    private fun requestVoiceCommand() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio)
            return
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
        if (spoken.isBlank()) return
        if (!handleLocalCommand(spoken.lowercase(Locale.ROOT))) askCloud(spoken)
    }

    private fun handleLocalCommand(spoken: String): Boolean {
        val apps = listOf(
            listOf("whatsapp", "व्हाट्सएप", "व्हाट्स ऐप") to ("WhatsApp" to "com.whatsapp"),
            listOf("instagram", "इंस्टाग्राम") to ("Instagram" to "com.instagram.android"),
            listOf("youtube", "यूट्यूब") to ("YouTube" to "com.google.android.youtube")
        )
        val match = apps.firstOrNull { entry -> entry.first.any { spoken.contains(it) } }?.second
            ?: return false
        val launch = packageManager.getLaunchIntentForPackage(match.second)
        if (launch != null) {
            startActivity(launch)
            status.text = "OPENING ${match.first.uppercase(Locale.ROOT)}"
            speakHindi("${match.first} खोल रहा हूँ।")
        } else {
            status.text = "${match.first.uppercase(Locale.ROOT)} NOT INSTALLED"
            speakHindi("${match.first} इस फोन में इंस्टॉल नहीं है।")
        }
        return true
    }

    private fun askCloud(message: String) {
        val endpoint = endpointInput.text.toString().trim().trimEnd('/')
        val clientKey = keyInput.text.toString().trim()
        if (!endpoint.startsWith("https://") || !endpoint.contains("workers.dev")) {
            status.text = "WORKER URL REQUIRED"
            Toast.makeText(this, "Cloudflare Worker का पूरा https URL भरें।", Toast.LENGTH_LONG).show()
            return
        }
        if (clientKey.isBlank()) {
            status.text = "CLIENT KEY REQUIRED"
            Toast.makeText(this, "JARVIS client key भरें।", Toast.LENGTH_LONG).show()
            return
        }
        prefs.edit().putString("endpoint", endpoint).putString("client_key", clientKey).apply()
        status.text = "CONNECTING TO CLOUD AI…"
        networkExecutor.execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$endpoint/chat")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 45000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("x-jarvis-client-key", clientKey)
                }
                val payload = JSONObject().put(
                    "messages",
                    JSONArray().put(JSONObject().put("role", "user").put("content", message))
                ).toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = JSONObject(responseText.ifBlank { "{}" })
                if (code in 200..299) {
                    val reply = response.optString("reply").trim()
                    if (reply.isBlank()) throw IllegalStateException("AI ने खाली जवाब भेजा।")
                    runOnUiThread {
                        command.text = "आप: $message\n\nJARVIS: $reply"
                        status.text = "CLOUD AI CONNECTED · ${response.optString("model", "AI")}"
                        speakHindi(reply)
                    }
                } else {
                    val error = response.optString("error", "Request failed")
                    runOnUiThread {
                        status.text = "AI ERROR · HTTP $code"
                        command.text = "JARVIS: $error"
                        speakHindi("क्लाउड AI से जवाब नहीं मिला।")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = "CONNECTION FAILED"
                    command.text = "कनेक्शन नहीं हुआ: ${e.message ?: "नेटवर्क या URL जाँचें"}"
                    speakHindi("क्लाउड कनेक्शन नहीं हो पाया।")
                }
            } finally {
                connection?.disconnect()
            }
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
        networkExecutor.shutdownNow()
        voiceEngine?.release()
        voiceEngine = null
        super.onDestroy()
    }
}
