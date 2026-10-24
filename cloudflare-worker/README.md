# JARVIS Cloud Brain — isolated test backend

This folder is a **separate Cloudflare Worker**, not GrowthPilot and not the existing Supabase project. It keeps the Hugging Face token on the server and never asks the Android app to contain that provider token.

## Important free-tier limitation

The Hugging Face model page currently lists Featherless AI as an inference provider. Hugging Face's published free-user allowance is currently $0.10/month for Inference Providers and may change. This is enough only for a small test if the selected model/provider accepts the request; it is not a promise of ongoing, unlimited free use. This Worker does not create a paid Hugging Face endpoint. If the model/provider is unavailable under free credits, the request should fail rather than silently switching to a paid service.

Model: `OBLITERATUS/Qwen3.8-27B-OBLITERATED`

## Deploy as a new Worker (do not reuse any existing Worker)

1. Create a **new** Cloudflare Worker named `jarvis-cloud-brain`.
2. From this folder, deploy with Wrangler, or paste `src/index.js` into the Worker editor.
3. Add Worker secrets in Cloudflare:
   - `HF_TOKEN`: a Hugging Face token with inference permissions.
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
{"reply":"...","model":"OBLITERATUS/Qwen3.8-27B-OBLITERATED"}
```

The Worker caps message size, message count, and response length. It intentionally does not store chat history or Drive files yet.

## Security note before app integration

A shared client key embedded in a mobile APK can be extracted. Do not treat it as strong user authentication or publish the endpoint widely. Before connecting the Android app for regular use, add an appropriate per-user authentication/rate-limit layer. The current branch is a test foundation only; it is not deployed and does not alter `main`.
