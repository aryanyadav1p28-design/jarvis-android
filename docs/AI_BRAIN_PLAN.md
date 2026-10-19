# J.A.R.V.I.S. AI Brain — Preparation Plan

This is a preparation branch. It intentionally does not change the working Android UI or the main branch.

## Known constraints
- Current Android project uses Android SpeechRecognizer with hi-IN and Android TextToSpeech.
- Current command handling opens supported apps; general AI replies are not integrated yet.
- Target computer: Windows 10, Intel i3, 4 GB RAM.
- Budget: free tools first; no paid API.
- Google Drive is optional document/memory storage, not an AI model.
- Keep the existing Android interface and app-opening behavior intact.

## Build order
1. On the laptop, inspect Windows version, CPU generation, free disk space, and whether Windows is 64-bit.
2. Test a very small local model before choosing it. A 4 GB RAM machine may be slow or unable to run some models; do not promise performance before testing.
3. Add a separate local AI bridge/service and verify Hindi/Hinglish text prompts first.
4. Add conversation memory locally with explicit clear/export controls.
5. Add optional Google Drive document retrieval only after the user signs in and grants permission. Never store API secrets in source code.
6. Connect Android to the AI service only over a deliberately configured secure connection. A laptop localhost address is not reachable from the phone automatically; same-Wi-Fi/LAN setup or a later hosted bridge needs configuration.
7. Keep speech recognition and TextToSpeech as fallbacks if AI is offline.
8. Require confirmation before sending messages, deleting files, or performing other external/destructive actions.
9. Run build and manual device tests before merging anything into main.

## Google Drive memory notes
- Drive files can hold user-approved notes/documents, but Drive does not itself provide reasoning or model inference.
- Use least-privilege OAuth scopes and user consent. Do not ask the user to paste passwords or OAuth tokens into chat or commit them to Git.
- Provide a way to inspect, edit, and delete stored memory.

## Acceptance checks
- Existing home screen and “I'M READY” / “J.A.R.V.I.S.” labels remain unchanged.
- Existing WhatsApp/Instagram/YouTube app-opening commands continue working.
- Offline/basic voice commands still work when the model is stopped.
- Hindi/Hinglish question produces a response only when the selected model is running.
- Memory retrieval uses only approved files/notes and can be cleared.
- No paid API, secret committed, or automatic message sending without confirmation.

## Current status
Planning only. The local model, Drive OAuth, AI bridge, and Android integration have not been implemented or device-tested yet.
