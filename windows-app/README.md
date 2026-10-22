# JARVIS Desktop for Windows

This is a separate Electron desktop-app scaffold on branch `jarvis-windows-app`. It does not modify Android app files, GrowthPilot, or any Supabase project.

## Build on Windows 10
1. Install a currently supported Node.js LTS release from https://nodejs.org/en/download
2. Open Command Prompt in this `windows-app` folder.
3. Run `npm install`
4. Run `npm start` to test the desktop window.
5. Run `npm run dist` to package a portable Windows executable into `dist`.

## Current features
- Separate Electron application window (not a website tab)
- Allowlisted links opened through the operating system's default browser
- Allowlisted Windows apps: Notepad, Calculator, Paint, File Explorer, Task Manager
- Spoken response where Windows/Electron speech synthesis is available
- "Hey Google" / "Hey Jarvis" phrase detection is attempted through Chromium Web Speech Recognition; it may require the online speech service to be reachable

## Limitations
- Do not assume speech recognition works on every Windows/Electron setup; test on the target laptop.
- Always-listening wake phrase reliability depends on the target Windows/Electron speech service.
- No automatic startup registration, cloud AI, Android sync, screen reading, or automatic message sending yet.
- Only allowlisted commands execute; arbitrary shell commands are deliberately not accepted.
- Test the packaged executable on Windows 10 before daily use.

## Release note
- A successful GitHub Actions build creates a versioned installer and `latest.yml` for the auto-updater. A successful build alone does not prove that in-app update detection works on every installed client.
