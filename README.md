# JARVIS Android

A separate Android app project, independent from GrowthPilot.

## Implemented in the cloud-ai-brain branch

- Native Android app module using Kotlin.
- Voice command entry point using Android speech recognition, configured for Hindi (hi-IN).
- Hindi spoken responses through the phone's installed Android Text-to-Speech voice.
- Local app-launch commands for WhatsApp, Instagram, and YouTube.
- Text chat and voice-to-cloud chat request support in the Android UI.
- Configurable Cloudflare Worker URL and JARVIS client key fields in the app.
- Cloud requests use HTTPS and send the client key in the request header, not in the URL.
- The Ollama provider key stays in the Cloudflare Worker and is not included in Android code.
- Runtime microphone permission request.

## Cloud backend

The isolated Worker source is in `cloudflare-worker/src/index.js`. It currently reports model `gemma4:31b` and calls Ollama Cloud's OpenAI-compatible chat endpoint.

The Android UI expects the base Worker URL (for example, your own `https://<your-worker>.workers.dev` address) and the same `JARVIS_CLIENT_KEY` configured as a Worker secret. Do not paste secrets into GitHub or chat. If a client key has been shared publicly, rotate it in Cloudflare and enter the new matching value in the app.

The client key saved in the app's local preferences is not a strong authentication boundary. A key embedded or stored on a mobile device can be extracted. This is a personal MVP connection, not production-grade authentication; add per-user auth and rate limiting before wider release.

## Important free-tier limitation

Ollama Cloud usage limits and plan terms can change; an API key alone does not guarantee unlimited free inference. The selected model is `gemma4:31b`, not the previously requested OBLITERATUS model.

## Not implemented yet

- Exact voice matching to the user's supplied audio. Android uses the device's installed Hindi TTS voice.
- Persistent conversation memory.
- Google Drive storage, learning, and document retrieval.
- Image/video file uploads to the cloud brain.
- Screen sharing, accessibility-based screen reading, or screenshots.
- Screen-off wake-word listening.
- Message reading/reply automation or media downloader integration.
- A verified APK build. The Android app must still be built and tested on a real device.

## Build and test

Open the repository in Android Studio and build the `:app` module. The current changes have been committed to `cloud-ai-brain`; no APK build or real-device test has been verified from this environment.

## Separation

Do not connect this app to the GrowthPilot Supabase project. This project must remain independent.
