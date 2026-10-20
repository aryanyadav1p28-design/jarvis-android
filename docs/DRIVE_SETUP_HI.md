# JARVIS Drive and stable signing setup

This branch is preparation work. Do not merge to `main` until the APK builds and is tested.

## Drive memory
- The app requests only `https://www.googleapis.com/auth/drive.file`.
- Sign in through the in-app Google Drive Connect button.
- Memory categories are Personal, Projects, Learned_Knowledge, and Important_Notes.
- Each save creates a new text file named `JARVIS_Memory_<category>.txt` in the signed-in user's Drive. Read opens the newest matching file.
- The access token is kept in memory only; it is not written to disk or source control.
- Google Auth Platform must have the scope and the user's Google account listed as a test user while the consent screen is in Testing.
- The Android OAuth client must use package `com.jarvis.assistant` and the SHA-1 for the certificate that actually signs the installed APK.

## Stable signing key (one-time owner setup)
The repository must never contain the keystore or its passwords. GitHub Actions uses these repository Actions secrets if configured:
- `JARVIS_KEYSTORE_BASE64`: base64 encoding of the JKS keystore bytes
- `JARVIS_KEYSTORE_PASSWORD`: keystore password
- `JARVIS_KEY_ALIAS`: key alias
- `JARVIS_KEY_PASSWORD`: key password

Create a dedicated JKS key on a trusted computer and keep an offline backup. Convert its bytes to base64 locally, then add the four values under GitHub repository Settings → Secrets and variables → Actions → New repository secret. Never paste the private key or passwords into chat, an issue, or a committed file. Once the secret is configured, the workflow uses the same signing certificate each time and prints its SHA-1 for the Android OAuth client.

If the secrets are not configured, the workflow intentionally falls back to a temporary debug key and warns that its SHA-1 is not stable. Do not use that temporary fingerprint for a long-term OAuth setup.

## Build verification checklist
- Run the Android build workflow manually on `jarvis-ai-prep`.
- Confirm Gradle succeeds and inspect the generated APK.
- Install on a test device; confirm existing Hindi voice and app-launch commands still work.
- Sign in to Drive, save a note to each category, then read it back.
- Confirm no secrets or tokens appear in repository files or build logs.
