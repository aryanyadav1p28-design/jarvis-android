package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color
import android.view.ViewGroup
import android.content.ActivityNotFoundException
import android.content.IntentSender
import android.widget.Toast
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var command: TextView
    private lateinit var memoryInput: EditText
    private lateinit var categorySpinner: Spinner
    private var voiceEngine: JarvisVoiceEngine? = null
    private var voiceReady = false
    private var driveAccessToken: String? = null
    private var driveMemory: DriveMemoryManager? = null
    private lateinit var learningLibrary: LearningLibrary
    private val requestAudio = 1001
    private val requestSpeech = 1002
    private val requestDriveAuthorization = 1003
    private val requestPdfImport = 1004
    private val driveScope = "https://www.googleapis.com/auth/drive"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(3, 10, 22)
        window.navigationBarColor = Color.rgb(3, 10, 22)
        learningLibrary = LearningLibrary(this)
        driveMemory = DriveMemoryManager { message ->
            runOnUiThread {
                status.text = message.take(220)
                Toast.makeText(this, message.take(220), Toast.LENGTH_LONG).show()
            }
        }
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
            setPadding(28, 24, 28, 24)
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
            text = "SYSTEM READY"; textSize = 14f; gravity = Gravity.CENTER
            setTextColor(Color.rgb(160, 205, 224))
            setPadding(0, 10, 0, 10)
        }
        command = TextView(this).apply {
            text = "माइक दबाकर हिंदी में बात करें।"; textSize = 16f
            gravity = Gravity.CENTER; setTextColor(Color.rgb(190, 216, 231))
            setPadding(12, 10, 12, 12)
        }
        val mic = Button(this).apply {
            text = "🎙  JARVIS से बात करें"; textSize = 16f
            setOnClickListener { requestVoiceCommand() }
        }
        val driveConnect = Button(this).apply {
            text = "Google Drive Connect"
            setOnClickListener { authorizeDrive() }
        }
        val memoryTitle = TextView(this).apply {
            text = "JARVIS MEMORY"; textSize = 16f; gravity = Gravity.CENTER
            setTextColor(Color.rgb(103, 232, 249))
            setPadding(0, 12, 0, 4)
        }
        categorySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Personal", "Projects", "Learned_Knowledge", "Important_Notes")
            )
        }
        memoryInput = EditText(this).apply {
            hint = "Yahan memory note likho..."
            minLines = 2
            gravity = Gravity.TOP
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(12, 10, 12, 10)
        }

        val teachButton = Button(this).apply {
            text = "Is text se JARVIS ko sikhao"
            setOnClickListener {
                try {
                    val lesson = learningLibrary.teach(selectedCategory(), "User lesson", memoryInput.text.toString())
                    status.text = "सीख सेव हुई: " + lesson.title + " (local)"
                    speakHindi("सीखी हुई जानकारी सेव हो गई।")
                } catch (e: Exception) {
                    status.text = e.message ?: "Lesson save nahi hui"
                }
            }
        }
        val importPdf = Button(this).apply {
            text = "PDF se Sikhao"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                }
                startActivityForResult(intent, requestPdfImport)
            }
        }
        val searchLessons = Button(this).apply {
            text = "Knowledge Library Search"
            setOnClickListener {
                val found = learningLibrary.search(memoryInput.text.toString(), selectedCategory())
                status.text = if (found.isEmpty()) "Is category mein matching lesson nahi mila." else
                    "Found " + found.size + ": " + found.joinToString(" | ") { it.title + " — " + it.content.take(90) }
            }
        }
        val saveMemory = Button(this).apply {
            text = "Selected category mein Save"
            setOnClickListener {
                val token = driveAccessToken
                if (token == null) status.text = "पहले Google Drive Connect दबाएँ।"
                else driveMemory?.save(token, selectedCategory(), memoryInput.text.toString())
            }
        }
        val readMemory = Button(this).apply {
            text = "Selected category ki Memory Padho"
            setOnClickListener {
                val token = driveAccessToken
                if (token == null) status.text = "पहले Google Drive Connect दबाएँ।"
                else driveMemory?.readLatest(token, selectedCategory())
            }
        }
        listOf(title, name, status, command, mic, driveConnect, memoryTitle, categorySpinner, memoryInput, teachButton, importPdf, searchLessons, saveMemory, readMemory).forEach {
            root.addView(it, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        setContentView(root)
    }

    private fun selectedCategory(): String = categorySpinner.selectedItem?.toString() ?: "Personal"

    private fun authorizeDrive() {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(driveScope)))
            .build()
        Identity.getAuthorizationClient(this)
            .authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent!!.intentSender,
                            requestDriveAuthorization, null, 0, 0, 0
                        )
                    } catch (_: IntentSender.SendIntentException) {
                        status.text = "DRIVE AUTHORIZATION COULD NOT OPEN"
                    }
                } else {
                    driveAccessToken = result.accessToken
                    status.text = if (driveAccessToken.isNullOrBlank()) "DRIVE TOKEN NOT RECEIVED" else "GOOGLE DRIVE CONNECTED"
                    speakHindi("Google Drive connect ho gaya.")
                }
            }
            .addOnFailureListener { error ->
                status.text = "DRIVE AUTH FAILED: ${error.message ?: "try again"}".take(180)
            }
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
            startActivityForResult(intent, requestSpeech)
        } catch (_: ActivityNotFoundException) {
            status.text = "VOICE SERVICE UNAVAILABLE"
            speakHindi("वॉइस सेवा उपलब्ध नहीं है।")
        }
    }

    @Deprecated("Activity result callback retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestDriveAuthorization) {
            if (resultCode == RESULT_OK) {
                try {
                    val result = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(data)
                    driveAccessToken = result.accessToken
                    status.text = if (driveAccessToken.isNullOrBlank()) {
                        "DRIVE TOKEN NOT RECEIVED — TRY AGAIN; CHECK GOOGLE CLOUD OAUTH CONFIG"
                    } else {
                        "GOOGLE DRIVE AUTHORIZED — TEST SAVE/READ TO VERIFY STORAGE"
                    }
                    if (!driveAccessToken.isNullOrBlank()) speakHindi("Google Drive authorization ho gaya. Ab save aur read test karein.")
                } catch (e: Exception) {
                    driveAccessToken = null
                    status.text = ("DRIVE RESULT ERROR: " + (e.message ?: e.javaClass.simpleName)).take(220)
                    speakHindi("Google Drive authorization result padhne mein dikkat hui.")
                }
            } else {
                driveAccessToken = null
                status.text = "DRIVE AUTHORIZATION CANCELLED (RESULT CODE $resultCode)"
            }
            return
        }
        if (requestCode == requestPdfImport) {
            if (resultCode == RESULT_OK && data?.data != null) {
                try {
                    val lesson = learningLibrary.importPdf(data.data!!)
                    status.text = "PDF सीखकर local library में सेव: " + lesson.title
                    speakHindi("PDF se seekhi hui jaankari save ho gayi.")
                } catch (e: Exception) {
                    status.text = ("PDF import failed: " + (e.message ?: "unknown error")).take(220)
                }
            } else {
                status.text = "PDF import cancelled"
            }
            return
        }
        if (requestCode != requestSpeech || resultCode != RESULT_OK) return
        val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        command.text = spoken.ifBlank { "कमांड समझ नहीं आई।" }
        handleCommand(spoken.lowercase(Locale.ROOT))
    }

    private fun handleCommand(spoken: String) {
        // Voice-first Drive memory actions.
        if (listOf("read memory", "memory read", "पर्सनल मेमोरी पढ़ो", "मेमोरी पढ़ो", "ड्राइव मेमोरी पढ़ो").any { spoken.contains(it) }) {
            val token = driveAccessToken
            if (token.isNullOrBlank()) {
                status.text = "पहले Google Drive Connect करें।"
                speakHindi("पहले गूगल ड्राइव कनेक्ट करें।")
            } else {
                status.text = "DRIVE MEMORY पढ़ रहा हूँ..."
                driveMemory?.readLatest(token, selectedCategory())
            }
            return
        }
        if (listOf("save memory", "memory save", "मेमोरी सेव करो", "मेमोरी सेव कर दो", "ड्राइव में सेव करो").any { spoken.contains(it) }) {
            val token = driveAccessToken
            if (token.isNullOrBlank()) {
                status.text = "पहले Google Drive Connect करें।"
                speakHindi("पहले गूगल ड्राइव कनेक्ट करें।")
            } else if (memoryInput.text.toString().isBlank()) {
                status.text = "पहले memory box में text लिखें।"
                speakHindi("सेव करने से पहले मेमोरी बॉक्स में टेक्स्ट लिखें।")
            } else {
                status.text = "DRIVE MEMORY सेव कर रहा हूँ..."
                driveMemory?.save(token, selectedCategory(), memoryInput.text.toString())
            }
            return
        }
        // Search the spoken query on the web without requiring keyboard input.
        val searchPrefixes = listOf("search for ", "search ", "google search ", "google par search ", "गूगल पर खोजो ", "गूगल पर सर्च करो ", "सर्च करो ", "खोजो ")
        val searchPrefix = searchPrefixes.firstOrNull { spoken.startsWith(it) }
        if (searchPrefix != null) {
            val query = spoken.removePrefix(searchPrefix).trim()
            if (query.isBlank()) {
                status.text = "SEARCH QUERY EMPTY"
                speakHindi("क्या सर्च करना है, कृपया बताइए।")
            } else {
                val searchUrl = "https://www.google.com/search?q=" + android.net.Uri.encode(query)
                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(searchUrl)))
                status.text = "GOOGLE SEARCH: ${query}"
                speakHindi("गूगल पर खोज रहा हूँ: ${query}")
            }
            return
        }
        if (listOf("connect drive", "drive connect", "गूगल ड्राइव जोड़", "ड्राइव कनेक्ट").any { spoken.contains(it) }) {
            status.text = "GOOGLE DRIVE AUTHORIZATION OPENING"
            authorizeDrive()
            return
        }
        if (listOf("read screen", "screen read", "पढ़ो स्क्रीन", "स्क्रीन पढ़ो", "स्क्रीन क्या है").any { spoken.contains(it) }) {
            val screenText = JarvisScreenReaderService.latestScreenText
            if (screenText.isNullOrBlank()) {
                status.text = "SCREEN TEXT NOT AVAILABLE — ENABLE JARVIS SCREEN READER IN ACCESSIBILITY SETTINGS"
                speakHindi("स्क्रीन का टेक्स्ट उपलब्ध नहीं है। पहले सेटिंग में जाकर जार्विस स्क्रीन रीडर की अनुमति चालू करें।")
            } else {
                val spokenText = screenText.take(3500)
                status.text = "SCREEN TEXT READ (${spokenText.length} characters)"
                speakHindi(spokenText)
            }
            return
        }
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
        driveMemory?.close()
        driveMemory = null
        super.onDestroy()
    }
}
