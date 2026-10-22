# JARVIS Desktop Companion

This folder contains a browser-based Windows/macOS/Linux laptop companion. It is intentionally separate from the native Android app so existing Android code and build settings remain unchanged.

## Run on a laptop
1. Download this repository branch or open `desktop/index.html` from the repository.
2. On a Windows laptop, double-click `desktop/index.html` to open it in Microsoft Edge or Google Chrome.
3. Allow microphone permission if prompted. If voice recognition is unavailable when opening the file directly, use the typing box; browser speech recognition support varies by browser and environment.
4. Use Quick Launch buttons or commands such as `YouTube kholo`, `WhatsApp kholo`, `Time batao`, `Help`, or `Search weather`.

No install, API key, or paid service is required for this starter.

## Current scope and limitations
- Hindi voice input uses the browser Speech Recognition API where available.
- Spoken replies use the browser's installed Hindi text-to-speech voice where available.
- Website shortcuts open in a new browser tab.
- This is **not yet a full AI assistant**: no AI backend, Android-to-laptop sync, Google Drive memory sync, always-on wake word, screen reading, screenshot analysis, or automatic message sending.
- Opening websites depends on the browser and network. Some browsers block popups or don't support speech recognition.
- Do not put secret API keys in this static HTML file.

## Safety and project separation
This is a new desktop-only folder on the `jarvis-desktop-companion` branch. The `main` branch and Android app configuration have not been changed by this desktop addition. Merge only after reviewing and testing both the desktop page and Android build.
