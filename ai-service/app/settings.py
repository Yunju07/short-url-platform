from pydantic import Field
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # AI service settings
    provider: str = Field(default="ollama", validation_alias="AI_PROVIDER")
    timeout_seconds: float = Field(default=10.0, validation_alias="AI_TIMEOUT_SECONDS")
    max_attempts: int = Field(default=3, validation_alias="AI_MAX_ATTEMPTS")

    # Ollama provider settings
    ollama_base_url: str = Field(default="http://ollama:11434", validation_alias="OLLAMA_BASE_URL")
    ollama_model: str = Field(default="phi3:mini", validation_alias="OLLAMA_MODEL")


settings = Settings()

