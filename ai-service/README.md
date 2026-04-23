# ai-service

Standard AI API service for the platform.

- Exposes a provider-agnostic API for generating short keys.
- Owns provider/model details (Ollama now, vLLM later).

## Local run (dev)

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

