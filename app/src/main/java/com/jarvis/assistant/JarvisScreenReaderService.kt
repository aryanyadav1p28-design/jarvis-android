package com.jarvis.assistant

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Opt-in screen text reader. The user must explicitly enable this service in Android Settings.
 * Text is kept in memory only and is not uploaded or logged.
 */
class JarvisScreenReaderService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val root = rootInActiveWindow ?: return
        val collected = StringBuilder()
        collectText(root, collected, 0)
        val text = collected.toString().trim()
        if (text.isNotEmpty()) latestScreenText = text.take(MAX_SCREEN_CHARS)
    }

    private fun collectText(node: AccessibilityNodeInfo?, out: StringBuilder, depth: Int) {
        if (node == null || depth > 35 || out.length >= MAX_SCREEN_CHARS) return
        val visibleText = node.text?.toString()?.trim().orEmpty()
        if (visibleText.isNotEmpty() && out.length < MAX_SCREEN_CHARS) {
            if (out.isNotEmpty()) out.append(". ")
            out.append(visibleText)
        }
        for (i in 0 until node.childCount) {
            if (out.length >= MAX_SCREEN_CHARS) break
            collectText(node.getChild(i), out, depth + 1)
        }
    }

    override fun onInterrupt() = Unit

    companion object {
        @Volatile
        var latestScreenText: String? = null
            private set
        private const val MAX_SCREEN_CHARS = 12000
    }
}