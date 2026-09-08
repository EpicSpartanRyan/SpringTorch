FROM ghcr.io/astral-sh/uv:python3.14-bookworm-slim

RUN apt-get update \
	&& apt-get install -y --no-install-recommends build-essential \
	&& rm -rf /var/lib/apt/lists/*

WORKDIR /app

ENV HF_HOME=/models

COPY pyproject.toml .
COPY uv.lock .
COPY main.py .

RUN uv sync --no-dev

CMD ["uv", "run", "main.py"]
