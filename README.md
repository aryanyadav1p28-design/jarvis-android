# JARVIS Android

A separate Android app project, independent from GrowthPilot.

## Starter implemented
- Native Android app module using Kotlin.
- Voice command entry point using Android speech recognition, configured for Hindi (`hi-IN`).
- Hindi spoken responses through the phone's installed Android Text-to-Speech voice.
- Slightly slower speech rate and adjusted pitch for a more conversational delivery; this is not a cloned/custom voice.
- Initial commands to open WhatsApp, Instagram, and YouTube when installed, including common Hindi app names.
- Runtime microphone permission request.
- J.A.R.V.I.S. label directly below "I'M READY".

## Approved robot video
The approved robot MP4 is preserved in the standalone HTML interface, but it has **not yet been added as a binary asset to this native Android repository**. Do not substitute a still image or another video. The exact MP4 must be added to `app/src/main/assets/jarvis_robot.mp4` and then wired into the Android UI.

## Not implemented yet
- Exact voice matching to the user's supplied audio. Android currently uses the device's installed Hindi TTS voice.
- Automatic multilingual recognition/replies. Input is currently set to Hindi; available recognition languages depend on the phone's speech service.
- Screen-off wake-word listening.
- Real AI conversation backend.
- Accessibility-based screen reading and screenshots.
- Message reading/reply automation.
- Media downloader / InDown integration.
- Separate Supabase integration.

## Build and test
Open the repository in Android Studio and build the `:app` module. A real-device build and permission testing are still required. No verified APK is included yet.

## Separation
Do not connect this app to the GrowthPilot Supabase project. Any future backend must be a separate project.
