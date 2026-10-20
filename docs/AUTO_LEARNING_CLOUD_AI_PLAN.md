# J.A.R.V.I.S. — Auto-Learning and Cloud AI Implementation Plan

## Product behavior
The user teaches without writing code. Inputs should include typed lessons, Android document picker for PDFs/text files, and a cloud file picker. For supported sources: import -> extract text -> preview/source -> create concise categorized notes -> user reviews/approves -> store locally and/or in Drive -> retrieve relevant notes during future tasks. Keep provenance and edit/delete controls.

## AI boundary
Knowledge retrieval is not model training. A configured AI inference endpoint is needed to summarize/structure lessons, answer questions grounded in them, and generate professional web/app code. Keep provider access behind an interface. Do not embed API keys in APK/source code; use a secure backend or user-supplied secure configuration. Without an endpoint, report that AI generation is not connected.

## Source handling
- PDF: extract selectable text; scanned/image-only PDFs need OCR and must be reported as unsupported unless implemented.
- Cloud: use Android document picker/Drive picker within granted permissions. The current Drive `drive.file` scope is not unrestricted Drive access.
- Web/YouTube: import only when page/transcript text is actually obtained; a URL alone is not learned content.
- Show import preview, limits, errors, and source metadata. Do not silently upload private source content to third-party AI.
- Deduplicate notes and retain source/date provenance.

## Code generation and execution
Generate code in a dedicated workspace, inspect diffs, run available tests, and present results. Ask before deployment, overwriting files, installing tools, external messages, or system changes. A later Windows bridge must authenticate over a trusted network, restrict working directory/resources, and never expose a raw shell publicly.

## Readiness checklist
1. Verify dependencies, manifest permissions, package ID, branch, and signing strategy.
2. Implement import, preview, extraction, review, categories/provenance, persistent knowledge storage.
3. Implement AI-provider interface with secure configuration and truthful disconnected state.
4. Test retrieval grounding with a known lesson and source references.
5. Test Drive auth and sample read/write on a device; match OAuth signing fingerprint.
6. Build APK, inspect logs/artifact; manual tests still needed for microphone, OAuth, PDF, and background behavior.
7. Keep `main` unchanged until user review.

## Current known gaps
Existing app has tap-to-speak Hindi recognition, app-launch commands for a small fixed list, Android TTS, and basic Drive memory save/read UI. It does not yet have a working AI endpoint, PDF text extraction, full auto-learning pipeline, wake word, notification/call handling, reminders, Windows bridge, or complete robot interface integration.
