# J.A.R.V.I.S. setup on Windows 10 (Hindi/Hinglish)

Use this checklist when the laptop arrives. Do not install a large model until the hardware and available disk space are checked.

## Step 1 — Inspect the laptop
1. Press Windows key + R.
2. Type `msinfo32` and press Enter.
3. Note **System Type**, **Processor**, and **Installed Physical Memory (RAM)**.
4. Open Settings → System → Storage and check free space.
5. Do not share serial numbers, product IDs, passwords, or personal files.

## Step 2 — Keep the project safe
- Do not edit the `main` branch directly.
- Use the separate `jarvis-ai-prep` branch for AI work.
- Keep a copy of the current APK and project before any integration.
- Build and test the Android app before merging changes.

## Step 3 — Select the local model carefully
- 4 GB RAM is very limited. Start by checking the smallest suitable quantized model and its actual RAM requirement.
- Close unnecessary apps during a test.
- If the laptop becomes unresponsive or inference is impractically slow, stop and use a lighter alternative rather than forcing installation.
- Do not promise fully offline, fast, high-quality Hindi conversation until tested on this machine.

## Step 4 — Prove text AI works first
1. Install only the selected tool from its official website.
2. Run one small model and test a short Hindi question.
3. Record approximate response time and memory use.
4. If successful, create a small local bridge/API for the Android app; keep it local/private during initial testing.

## Step 5 — Add memory
- Begin with a simple local notes file or database and explicit save/forget controls.
- Add Google Drive only after local memory works.
- Sign in through the official OAuth consent flow; never put client secrets or tokens in source code.
- Test with a harmless sample document first.

## Step 6 — Connect the Android app
- Android's `localhost` refers to the phone itself, not the laptop.
- For phone-to-laptop testing, configure the laptop's private LAN address and firewall carefully, use the same trusted Wi-Fi, and do not expose the local AI port to the public internet.
- Keep existing speech recognition and text-to-speech behavior as fallbacks.
- Ask for confirmation before sending messages or doing destructive actions.

## Do not do
- Do not change GrowthPilot OS or its Supabase project.
- Do not commit passwords, API keys, OAuth tokens, or personal Drive contents.
- Do not merge untested AI changes into `main`.
