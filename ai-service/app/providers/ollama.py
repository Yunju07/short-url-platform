import httpx


class OllamaClient:
    def __init__(self, base_url: str, model: str, timeout_seconds: float):
        self._base_url = base_url.rstrip("/")
        self._model = model
        self._timeout = timeout_seconds

    @property
    def model(self) -> str:
        return self._model

    async def generate(self, prompt: str) -> str:
        url = f"{self._base_url}/api/generate"
        # Keep the response short and stop at the first newline to avoid slow generations.
        payload = {
            "model": self._model,
            "prompt": prompt,
            "stream": False,
            "options": {
                "num_predict": 32,
                "temperature": 0.2,
                "stop": ["\n"],
            },
        }

        async with httpx.AsyncClient(timeout=self._timeout) as client:
            res = await client.post(url, json=payload)
            res.raise_for_status()

        data = res.json()
        # Ollama returns: { model, created_at, response, done, ... }
        return (data.get("response") or "").strip()
