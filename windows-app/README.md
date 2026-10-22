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
- Hindi speech recognition if the runtime exposes Web Speech Recognition

## Limitations
- Do not assume speech recognition works on every Windows/Electron setup; test on the target laptop.
- No wake-word always-listening, automatic startup registration, cloud AI, Android sync, screen reading, or automatic message sending yet.
- Only allowlisted commands execute; arbitrary shell commands are deliberately not accepted.
- Test the packaged executable on Windows 10 before daily use.
