# J.A.R.V.I.S. — User Requirements (Confirmed)

## Branch and release safety
- Work on `jarvis-ai-prep`; keep `main` unchanged until the user reviews a tested result.
- The user has explicitly asked for the next deliverable to be an APK after implementation and build checks.
- Never describe a requirement as implemented until code and tests verify it.

## Auto-learning and cloud knowledge
- Provide a user-facing learning flow that does not require the user to write code: teach by text, documents/PDFs, and user-selected cloud files; allow web/source links where technically supported.
- Preserve source title, source type, import date, and relevant source URL/file reference alongside extracted lesson notes.
- Extract useful concepts into searchable, categorized knowledge; deduplicate overlapping notes, distinguish source facts from AI inference, and let the user review/edit/delete learned notes.
- At answer/task time, retrieve relevant saved lessons and use them as context. Learning must persist across sessions and be reusable by Android and later Windows 10 client.
- Treat learning as retrieval/knowledge memory, not a claim that the underlying model was retrained. Professional web/app development requires a configured cloud AI model/backend capable of code generation and analysis.
- Do not send private PDFs or Drive content to an AI provider without explaining the data flow and getting consent. Never save passwords, API keys, OAuth tokens, or unrelated secrets.
- Existing Drive scope `drive.file` is limited to files created/opened with the app; it does not grant blanket access to all Drive files. Use a picker or explain any wider scope.
- Prefer free tiers and disclose limits. If AI endpoint/API setup is missing, show the feature as not connected rather than pretending it works.

## Memory and cleanup
- Keep categories: Personal, Projects, Learned Knowledge, Important Notes.
- Normal/temp data older than 5 days may be cleaned up only after the exact normal-data types are defined and previewed.
- Important files must never be auto-deleted; show candidates and ask explicit confirmation. Use recoverable trash where supported.
- Avoid duplicate uploads and keep concise notes rather than full conversations. Cleanup applies only to data the app can identify and has permission to manage.

## Assistant capabilities requested
- Hindi voice interaction and configurable robot voice when supported.
- Wake-word listening only after explicit permission, with foreground-service notification and clear mic state; disclose Android battery/background limits.
- Open apps and supported tasks; ask before consequential external actions, sending messages, deleting important files, or changing system settings.
- Reminders via Android notifications/alarms with exact-alarm/battery restrictions disclosed.
- Notification reading, WhatsApp responses, call handling, and recordings/transcripts require respective Android permissions, supported APIs, user controls, and applicable consent. Never imply they work before end-to-end tests.
- Modular tools that can later use Termux or a restricted Windows 10 execution bridge. Never expose a shell port to the public internet.
- Inspect the user's robot-interface HTML and assets before choosing an Android-compatible integration path.

## Ethical hacking learning focus
- Beginner-friendly web penetration testing and networking/Linux/network security: HTTP/HTTPS, browser/devtools, auth/session security, common web vulnerabilities, remediation/reporting, TCP/IP, DNS, ports, routing, Linux CLI, permissions, logs, firewalls, defensive analysis.
- Allow configurable labs, authorized target list, scan intensity, tools, and confirmation preferences without editing code.
- Show scope before active tests and confirm tool installation, system changes, and disruptive scans.
- Configurable preferences do not remove boundaries against unauthorized intrusion, credential theft, destructive activity, or indiscriminate public-target scanning.

## Quality and release
- Preserve existing UI and app-launch behavior. Prefer free options first.
- Run build/tests and report exact outcomes. A successful APK build does not prove all requested features work on a physical device.
