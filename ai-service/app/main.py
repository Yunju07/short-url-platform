import re
import uuid
from typing import Optional

import httpx
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from pydantic import AnyUrl, BaseModel, Field, model_validator

from app.settings import settings
from app.providers.ollama import OllamaClient


app = FastAPI(title="ai-service", version="0.1.0")


class ApiError(BaseModel):
    code: str
    message: str
    traceId: Optional[str] = None


class ShortKeyCreateRequest(BaseModel):
    originalUrl: AnyUrl
    minLength: int = Field(ge=1)
    maxLength: int = Field(ge=1)
    alphabet: str = Field(min_length=1)

    @model_validator(mode="after")
    def _validate_lengths(self):
        if self.minLength > self.maxLength:
            raise ValueError("minLength must be <= maxLength")
        return self

    @model_validator(mode="after")
    def _validate_alphabet(self):
        # v1 policy: lowercase + digits only
        if not re.fullmatch(r"[a-z0-9]+", self.alphabet):
            raise ValueError("alphabet must match [a-z0-9]+")
        # ensure uniqueness to avoid surprising filtering behavior
        if len(set(self.alphabet)) != len(self.alphabet):
            raise ValueError("alphabet must not contain duplicate characters")
        return self


class ShortKeyCreateResponse(BaseModel):
    shortKey: str
    provider: Optional[str] = None
    model: Optional[str] = None
    traceId: Optional[str] = None


def _trace_id() -> str:
    return uuid.uuid4().hex


@app.middleware("http")
async def add_trace_id(request: Request, call_next):
    request.state.trace_id = _trace_id()
    response = await call_next(request)
    response.headers["x-trace-id"] = request.state.trace_id
    return response


def _clean_candidate(raw: str, alphabet: str) -> str:
    # Keep only allowed characters.
    allowed = set(alphabet)
    lowered = (raw or "").strip().lower()
    return "".join([c for c in lowered if c in allowed])


def _build_prompt(original_url: str, min_len: int, max_len: int, alphabet: str) -> str:
    return (
        "You generate a shortKey for a URL.\n"
        "Requirements:\n"
        f"- characters allowed: {alphabet}\n"
        f"- length: {min_len} to {max_len}\n"
        "- output ONLY the shortKey\n"
        "\n"
        "Examples:\n"
        "URL: https://example.com/auth/login\n"
        "shortKey: login01\n"
        "\n"
        f"URL: {original_url}\n"
        "shortKey:"
    )


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    # Keep error shape stable.
    trace_id = getattr(request.state, "trace_id", None)
    return JSONResponse(
        status_code=500,
        content=ApiError(code="INTERNAL", message="unexpected error", traceId=trace_id).model_dump(),
    )


@app.post("/v1/shortkeys", response_model=ShortKeyCreateResponse)
async def create_shortkey(req: ShortKeyCreateRequest, request: Request):
    trace_id = request.state.trace_id

    if settings.provider != "ollama":
        return JSONResponse(
            status_code=500,
            content=ApiError(code="INTERNAL", message="unsupported provider", traceId=trace_id).model_dump(),
        )

    client = OllamaClient(
        base_url=settings.ollama_base_url,
        model=settings.ollama_model,
        timeout_seconds=settings.timeout_seconds,
    )

    prompt = _build_prompt(str(req.originalUrl), req.minLength, req.maxLength, req.alphabet)
    last_err: Optional[str] = None

    for _ in range(max(1, settings.max_attempts)):
        try:
            raw = await client.generate(prompt)
            cleaned = _clean_candidate(raw, req.alphabet)
            if req.minLength <= len(cleaned) <= req.maxLength:
                return ShortKeyCreateResponse(
                    shortKey=cleaned,
                    provider="ollama",
                    model=client.model,
                    traceId=trace_id,
                )
            last_err = "invalid output"
        except httpx.TimeoutException:
            last_err = "timeout"
        except httpx.HTTPError:
            last_err = "upstream failure"

    status = 502
    code = "UPSTREAM_FAILURE"
    msg = "provider request failed"
    if last_err == "timeout":
        status = 504
        code = "TIMEOUT"
        msg = "provider timeout"
    elif last_err == "invalid output":
        status = 502
        code = "INVALID_OUTPUT"
        msg = "provider returned invalid output"

    return JSONResponse(
        status_code=status,
        content=ApiError(code=code, message=msg, traceId=trace_id).model_dump(),
    )


@app.get("/health")
async def health():
    return {"ok": True}

