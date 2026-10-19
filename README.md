# JARVIS Android

A separate Android app project, independent from GrowthPilot.

## Starter implemented
- Native Android app module using Kotlin.
- Voice command entry point using Android speech recognition (currently configured for Hindi, `hi-IN`).
- Hindi spoken responses through the phone's installed Android text-to-speech voice.
- Initial commands to open WhatsApp, Instagram, and YouTube when installed, including common Hindi app names.
- Runtime microphone permission request.
- J.A.R.V.I.S. label directly below "I'M READY".

## Not implemented yet
- Approved robot video in the native Android app. The exact MP4 must be added as a binary asset and wired into the interface; do not substitute a still image or another video without the owner's explicit request.
- Matching the exact voice from the earlier browser interface; current voice uses the device's installed Hindi TTS voice.
- Multi-language auto-detection. Voice input is currently set to Hindi; supported recognition languages depend on the device's speech service.
- Screen-off wake-word listening.
- AI conversation backend.
- Accessibility-based screen reading and screenshots.
- Message reading/reply automation.
- Media downloader / InDown integration.
- Supabase integration.

## Build and test
Open the repository in Android Studio and build the `:app` module. A real-device build and permission testing are still required. No verified APK is included yet.

## Separation
Do not connect this app to the GrowthPilot Supabase project. Any future backend must be a separate project.
