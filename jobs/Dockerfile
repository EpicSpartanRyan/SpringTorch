FROM python:3.14-slim-bookworm

COPY --from=ghcr.io/astral-sh/uv:0.12.11 /uv /uvx /bin/

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
