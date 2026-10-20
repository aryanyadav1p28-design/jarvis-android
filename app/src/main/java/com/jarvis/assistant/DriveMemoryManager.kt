package com.jarvis.assistant

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Minimal Drive-backed memory store. Files are created by JARVIS in the user's
 * Drive and separated by category. Network work runs off the main thread.
 */
class DriveMemoryManager(
    private val onResult: (String) -> Unit
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val baseUrl = "https://www.googleapis.com/drive/v3/files"

    fun save(accessToken: String, category: String, note: String) {
        if (note.isBlank()) {
            onResult("पहले memory में सेव करने के लिए text लिखें।")
            return
        }
        executor.execute {
            try {
                val safeCategory = category.replace(Regex("[^A-Za-z0-9_-]"), "_")
                val fileName = "JARVIS_Memory_${safeCategory}.txt"
                val boundary = "jarvis-${UUID.randomUUID()}"
                val metadata = JSONObject()
                    .put("name", fileName)
                    .put("mimeType", "text/plain")
                val body = buildString {
                    append("--").append(boundary).append("\r\n")
                    append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
                    append(metadata.toString()).append("\r\n")
                    append("--").append(boundary).append("\r\n")
                    append("Content-Type: text/plain; charset=UTF-8\r\n\r\n")
                    append(note).append("\r\n")
                    append("--").append(boundary).append("--\r\n")
                }
                val connection = (URL("$baseUrl?uploadType=multipart&fields=id,name").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val response = readResponse(connection, code)
                connection.disconnect()
                if (code in 200..299) {
                    val fileId = runCatching { JSONObject(response).optString("id") }.getOrDefault("")
                    onResult("Drive में $category memory सेव हो गई।" + if (fileId.isNotBlank()) "\nFile ID: $fileId" else "")
                } else {
                    onResult("Drive save error ($code): ${friendlyError(response)}")
                }
            } catch (e: Exception) {
                onResult("Drive save नहीं हुआ: ${e.message ?: "network error"}")
            }
        }
    }

    fun readLatest(accessToken: String, category: String) {
        executor.execute {
            try {
                val safeCategory = category.replace(Regex("[^A-Za-z0-9_-]"), "_")
                val fileName = "JARVIS_Memory_${safeCategory}.txt"
                val query = java.net.URLEncoder.encode("name='$fileName' and trashed=false", "UTF-8")
                val listUrl = "$baseUrl?q=$query&orderBy=createdTime%20desc&pageSize=1&fields=files(id,name)"
                val listConnection = (URL(listUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                val listCode = listConnection.responseCode
                val listText = readResponse(listConnection, listCode)
                listConnection.disconnect()
                if (listCode !in 200..299) {
                    onResult("Drive read error ($listCode): ${friendlyError(listText)}")
                    return@execute
                }
                val files = JSONObject(listText).optJSONArray("files") ?: JSONArray()
                if (files.length() == 0) {
                    onResult("$category memory अभी Drive में नहीं मिली।")
                    return@execute
                }
                val id = files.getJSONObject(0).getString("id")
                val contentConnection = (URL("$baseUrl/$id?alt=media").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                val contentCode = contentConnection.responseCode
                val content = readResponse(contentConnection, contentCode)
                contentConnection.disconnect()
                if (contentCode in 200..299) onResult("$category memory:\n$content")
                else onResult("Memory download error ($contentCode): ${friendlyError(content)}")
            } catch (e: Exception) {
                onResult("Drive से पढ़ना असफल: ${e.message ?: "network error"}")
            }
        }
    }

    private fun friendlyError(raw: String): String {
        if (raw.isBlank()) return "Google Drive ने खाली error response दिया।"
        return try {
            val root = JSONObject(raw)
            val error = root.optJSONObject("error")
            val message = error?.optString("message").orEmpty()
            val reasons = error?.optJSONArray("errors")
            val reason = if (reasons != null && reasons.length() > 0) {
                reasons.optJSONObject(0)?.optString("reason").orEmpty()
            } else ""
            listOf(message, reason.takeIf { it.isNotBlank() }?.let { "reason=$it" })
                .filterNotNull()
                .filter { it.isNotBlank() }
                .joinToString(" | ")
                .ifBlank { raw.take(500) }
        } catch (_: Exception) {
            raw.take(500)
        }
    }

    private fun readResponse(connection: HttpURLConnection, code: Int): String {
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        return stream?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
        }.orEmpty()
    }

    fun close() {
        executor.shutdown()
    }
}
