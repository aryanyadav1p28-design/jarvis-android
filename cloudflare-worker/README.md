# JARVIS Cloud Brain — isolated test backend

This folder is a **separate Cloudflare Worker**, not GrowthPilot and not the existing Supabase project. It keeps the Hugging Face token on the server and never asks the Android app to contain that provider token.

## Important free-tier limitation

The Worker now uses OpenRouter's `qwen/qwen3.8-27b:free` endpoint, which is a free-hosted Qwen 3.8 27B model. This is **not** the `OBLITERATUS/Qwen3.8-27B-OBLITERATED` variant; the exact OBLITERATED variant did not have a reliably confirmed unlimited-free host. OpenRouter labels this endpoint free, but free endpoints can be rate-limited, busy, changed, or unavailable. Free access is not guaranteed forever. This setup makes no paid requests intentionally; do not add a payment method or purchase credits for this project.

Model: `qwen/qwen3.8-27b:free`

## Deploy as a new Worker (do not reuse any existing Worker)

1. Create a **new** Cloudflare Worker named `jarvis-cloud-brain`.
2. From this folder, deploy with Wrangler, or paste `src/index.js` into the Worker editor.
3. Add Worker secrets in Cloudflare:
   - `OPENROUTER_API_KEY`: an API key from OpenRouter. Use the free endpoint; do not buy credits or choose a paid model.
   - `JARVIS_CLIENT_KEY`: a long, random value used to authenticate requests.
4. Never put `HF_TOKEN` in the Android app, GitHub files, screenshots, or chat.
5. Test `GET /health`; it should return `ok: true`.
6. Test `POST /chat` using the `x-jarvis-client-key` header and a short message before connecting the Android UI.

## API

`POST /chat`

Headers:
- `content-type: application/json`
- `x-jarvis-client-key: <JARVIS_CLIENT_KEY>`

Body:
```json
{
  "messages": [
    {"role": "user", "content": "नमस्ते JARVIS"}
  ]
}
```

Successful response:
```json
{"reply":"...","model":"qwen/qwen3.8-27b:free"}
```

The Worker caps message size, message count, and response length. It intentionally does not store chat history or Drive files yet.

## Security note before app integration

A shared client key embedded in a mobile APK can be extracted. Do not treat it as strong user authentication or publish the endpoint widely. Before connecting the Android app for regular use, add an appropriate per-user authentication/rate-limit layer. The current branch is a test foundation only; it is not deployed and does not alter `main`.
