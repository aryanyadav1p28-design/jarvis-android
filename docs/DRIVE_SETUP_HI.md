# JARVIS Drive and stable signing setup

This branch is preparation work. Do not merge to `main` until the APK builds and is tested.

## Drive access and privacy
- JARVIS requests the broad Google Drive scope `https://www.googleapis.com/auth/drive`, not only `drive.file`.
- This scope can allow reading, creating, editing, and deleting files across the signed-in user's Drive. Use it only if you are comfortable granting that level of access.
- Google may require additional consent-screen configuration, verification, or testing-user setup before authorization succeeds. The app cannot bypass Google's consent.
- Sign in through the in-app Google Drive Connect button. The access token is kept in memory only; it is not written to disk or source control.
- Memory categories are Personal, Projects, Learned_Knowledge, and Important_Notes.
- Current memory actions save category text files and read the latest JARVIS category file. A full Drive file browser/search/delete UI is not implemented yet; broad scope alone does not create those features.
- Do not delete user files automatically. Any future delete workflow must show the selected file and ask for confirmation first.
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
- Sign in to Drive and confirm Google consent works with the broad Drive scope.
- Save a note to each category, then read it back.
- Confirm no secrets or tokens appear in repository files or build logs.
