# J.A.R.V.I.S. — Modular Architecture and Drive Memory Plan

Status: design document only. No Android app code, OAuth credentials, or Drive connection is changed by this document. Keep `main` unchanged.

## Goals
- Start with Google Drive as cloud storage for user-approved useful memories, lesson notes, and learning progress.
- Keep the Android app and Windows laptop able to use the same memory and future tools.
- Add tools as independent modules so future integrations (Termux, Linux shell, browser research, YouTube lesson importer) do not require rewriting the assistant core.
- Use free options first and show costs/limits before enabling any paid service.

## Proposed layers
1. **Android client**: current Hindi speech input and Android TextToSpeech remain as fallbacks; displays AI replies and asks for confirmation before consequential actions.
2. **Assistant core / router**: classifies a request, chooses an allowed tool, records the result, and returns a response. It must not silently run arbitrary generated shell commands.
3. **Memory service**: search approved notes before answering; propose a concise memory update after useful lessons/preferences; deduplicate and timestamp entries; allow review, export, edit, and delete.
4. **Storage adapter**: `MemoryStore` interface first, Google Drive adapter later. Keep local temporary cache optional. Do not sync all conversations by default; save useful information, redact secrets, and provide an audit log.
5. **Tool registry**: each integration declares a name, purpose, input schema, permissions, risk level, and whether user confirmation is required. Examples: YouTube transcript importer, web research, Termux bridge, Linux coding workspace.
6. **Execution bridge**: a separate local service on the Windows/Linux computer for code execution. Android must authenticate to it over a trusted network. Never expose a shell port directly to the public internet.

## Google Drive integration steps
1. User creates/selects a Google Cloud project and enables Google Drive API.
2. Configure OAuth consent and a suitable Android OAuth client using the package/signing details from the actual app build. Do not guess signing fingerprints.
3. Request the narrowest appropriate Drive scope and let the user sign in and consent on-device.
4. Store only OAuth tokens using platform-protected storage; never commit client secrets, access tokens, refresh tokens, or Drive files to Git.
5. Create an app-owned folder or clearly named memory file after consent; test with a harmless sample note.
6. Add search, append/update, conflict handling, offline error handling, and user controls to inspect/export/delete saved memory.
7. Verify that no Drive connection is claimed until OAuth succeeds and a read/write test passes.

## Memory behavior
- Automatically propose/save only useful lessons, stable preferences, and important project decisions.
- Do not save passwords, API keys, authentication tokens, financial identifiers, or unrelated sensitive information.
- Keep source links and dates for lessons. A YouTube URL alone is not proof that a video was watched; use an accessible transcript/captions or clearly report that access failed.
- Distinguish retrieved facts from model-generated guesses.

## Tool safety and Linux coding
- Coding can run in a dedicated workspace with an explicit working directory, time/resource limits, command logs, and a reviewable diff.
- Begin with read-only inspection and test commands. Ask before installing packages, changing system settings, deleting/overwriting files, accessing credentials, or running network scans.
- Prefer disposable VMs/containers and intentionally vulnerable labs for security practice.
- Support authorized ethical hacking (for example, own lab machines, CTFs, and explicitly authorized targets). Do not add a feature intended to break into other people's Wi-Fi or recover credentials from networks without permission. For a user's own Wi-Fi, prioritize router-admin recovery, ISP/router reset steps, and authorized security auditing rather than unattended password cracking.
- Termux is a separate Android environment; integrating it requires a clearly scoped bridge and user-granted permissions. It is not installed or connected by this plan.

## Implementation order
1. Confirm device/app package and build details without touching `main`.
2. Implement a small `MemoryStore` abstraction and tests using a local mock first.
3. Implement Google OAuth/Drive adapter on `jarvis-ai-prep`; test sign-in and sample read/write.
4. Add a modular tool registry with explicit permission and confirmation policies.
5. Add a laptop execution bridge in a restricted workspace; test harmless Python commands first.
6. Add YouTube transcript ingestion and source-linked lesson notes.
7. Build APK and run manual tests; compare UI and existing app-opening commands against baseline.
8. Merge only after the user reviews results and tests pass.

## Current state
Planning only. Google Drive is not connected yet. Gemini is intentionally deferred. No new tool integrations, Linux execution, or Wi-Fi audit implementation is present yet.
