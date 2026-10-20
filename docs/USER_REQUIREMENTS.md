# J.A.R.V.I.S. — User Requirements (Confirmed)

Branch: `jarvis-ai-prep` only. Do not modify `main`. Do not build or publish an APK unless the user separately gives explicit approval.

## Data cleanup and important files

- Automatically clean up **normal/temporary data older than 5 days**.
- **Important files must never be deleted automatically**. Before deleting an important file, show the file/category and ask for explicit confirmation.
- Keep memory categories separate (for example: Personal, Projects, Learned Knowledge, Important Notes).
- Keep Google Drive usage small: prefer concise, deduplicated notes over uploading full conversations or repeated copies.
- Do not store passwords, API keys, OAuth tokens, or other secrets in plaintext.
- Before implementing cleanup, define which categories count as normal vs important, preview eligible items, and ensure important items are excluded from automatic deletion. Use recoverable trash where supported and report failures; do not claim cleanup succeeded unless verified.
- Retention applies only to data the app can identify and has permission to manage. Android call/notification records and Drive files have different permissions and deletion behavior.

## Ethical hacking learning focus

Prioritize a structured, beginner-friendly curriculum covering both:
1. **Web penetration testing** — HTTP/HTTPS, browser/devtools, authentication/session security, common web vulnerabilities, safe testing, remediation, and reporting.
2. **Networking, Linux, and network security** — TCP/IP, DNS, ports, routing, Linux command line, permissions, logs, firewalls, and defensive network analysis.

- Add a **user-configurable testing profile/scope** so the user can choose and update their learning environment and authorized target list later, without editing code.
- Let the user configure ordinary preferences such as lab selection, scan intensity, tool choices, and confirmation prompts. Show the current scope before an active test and ask before installing tools, changing system settings, or running potentially disruptive scans.
- A configuration option must not turn the assistant into an unrestricted agent for unauthorized intrusion, credential theft, destructive activity, or indiscriminate public-target scanning. Keep those safety boundaries even if the user changes their profile.
- Prefer legal labs/CTFs and systems the user owns or has explicit permission to test. Support broad learning, defensive analysis, code review, and safe testing guidance.

## General safety and implementation constraints

- Keep modules separate so features can be added without rewriting the assistant core.
- Ask for confirmation before consequential external actions, sending messages, deleting important files, or changing system settings.
- Preserve existing UI and app-launch behavior.
- Prefer free tools and explain limitations before proposing paid services.
- Treat these as requirements, not proof of implementation. Test each feature and report results honestly.
- **No APK build or release without separate explicit user approval.**
