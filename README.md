# JARVIS Android

A separate Android app project, independent from GrowthPilot.

## Starter implemented
- Native Android app module using Kotlin.
- Voice command entry point using Android speech recognition.
- Initial commands to open WhatsApp, Instagram, and YouTube when installed.
- Runtime microphone permission request.
- J.A.R.V.I.S. label directly below "I'M READY".

## Not implemented yet
- Approved robot video: the exact video must be added as an Android asset and wired into the interface. Do not substitute a still image or different video without the owner's explicit request.
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
