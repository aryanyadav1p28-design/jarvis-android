package com.jarvis.assistant

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local-first knowledge library. This stores source-linked notes; it does not retrain an AI model.
 * PDF text extraction supports text-based PDFs. Scanned PDFs require OCR and are reported as empty.
 */
class LearningLibrary(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("jarvis_learning_library", Context.MODE_PRIVATE)

    init { PDFBoxResourceLoader.init(appContext) }

    data class Lesson(
        val id: String,
        val category: String,
        val title: String,
        val source: String,
        val createdAt: Long,
        val content: String
    )

    fun importPdf(uri: Uri): Lesson {
        val title = queryName(uri) ?: "Imported PDF"
        val text = appContext.contentResolver.openInputStream(uri)?.use { input ->
            PDDocument.load(input).use { doc -> PDFTextStripper().getText(doc) }
        }.orEmpty().trim()
        if (text.isBlank()) {
            throw IllegalArgumentException("PDF में selectable text नहीं मिला। यह scanned PDF हो सकती है; OCR अभी उपलब्ध नहीं है।")
        }
        return saveLesson("Learned_Knowledge", title, "PDF: $title", text)
    }

    fun teach(category: String, title: String, content: String): Lesson {
        if (content.isBlank()) throw IllegalArgumentException("पहले सीखने के लिए text लिखें।")
        return saveLesson(category, title.ifBlank { "User lesson" }, "User taught", content)
    }

    @Synchronized
    private fun saveLesson(category: String, title: String, source: String, content: String): Lesson {
        val normalized = content.trim().take(120_000)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((category + "|" + normalized).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val all = loadAll().toMutableList()
        val duplicate = all.firstOrNull {
            MessageDigest.getInstance("SHA-256")
                .digest((it.category + "|" + it.content.trim()).toByteArray(Charsets.UTF_8))
                .joinToString("") { b -> "%02x".format(b) } == digest
        }
        if (duplicate != null) return duplicate
        val lesson = Lesson(
            id = digest.take(20),
            category = category,
            title = title.take(160),
            source = source.take(300),
            createdAt = System.currentTimeMillis(),
            content = normalized
        )
        all.add(0, lesson)
        persist(all.take(300))
        return lesson
    }

    fun search(query: String, category: String? = null): List<Lesson> {
        val words = query.lowercase(Locale.ROOT).split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length > 1 }.toSet()
        if (words.isEmpty()) return loadAll().filter { category == null || it.category == category }.take(10)
        return loadAll().asSequence()
            .filter { category == null || it.category == category }
            .map { lesson ->
                val haystack = (lesson.title + " " + lesson.source + " " + lesson.content).lowercase(Locale.ROOT)
                lesson to words.count { haystack.contains(it) }
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(10)
            .map { it.first }
            .toList()
    }

    fun all(): List<Lesson> = loadAll()

    fun delete(id: String): Boolean {
        val old = loadAll()
        val next = old.filterNot { it.id == id }
        if (next.size == old.size) return false
        persist(next)
        return true
    }

    fun exportText(lessons: List<Lesson>): String = lessons.joinToString("\n\n---\n\n") {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(Date(it.createdAt))
        "Title: ${it.title}\nCategory: ${it.category}\nSource: ${it.source}\nSaved: $date\n\n${it.content}"
    }

    private fun loadAll(): List<Lesson> {
        val json = prefs.getString("lessons", "[]") ?: "[]"
        val array = try { JSONArray(json) } catch (_: Exception) { JSONArray() }
        return (0 until array.length()).mapNotNull { i ->
            try {
                val o = array.getJSONObject(i)
                Lesson(
                    id = o.getString("id"),
                    category = o.getString("category"),
                    title = o.getString("title"),
                    source = o.getString("source"),
                    createdAt = o.getLong("createdAt"),
                    content = o.getString("content")
                )
            } catch (_: Exception) { null }
        }
    }

    private fun persist(lessons: List<Lesson>) {
        val array = JSONArray()
        lessons.forEach { l ->
            array.put(JSONObject()
                .put("id", l.id)
                .put("category", l.category)
                .put("title", l.title)
                .put("source", l.source)
                .put("createdAt", l.createdAt)
                .put("content", l.content))
        }
        prefs.edit().putString("lessons", array.toString()).apply()
    }

    private fun queryName(uri: Uri): String? =
        appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
}
