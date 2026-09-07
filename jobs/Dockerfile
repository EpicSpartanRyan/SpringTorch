FROM ghcr.io/astral-sh/uv:python3.14-bookworm-slim

WORKDIR /app

COPY pyproject.toml .
COPY main.py .

RUN uv sync --no-dev

CMD ["uv", "run", "main.py"]
