# SpringTorch

SpringTorch is an application composed of a Spring Boot backend and a Python worker for running Qwen inference through Temporal.io.

Spring Boot exposes the HTTP API and orchestrates Workflow execution. The Python worker does not expose a web endpoint: it listens to a Temporal Task Queue and runs the inference Activity using PyTorch.

## Architecture

```text
HTTP Client
    |
    v
Spring Boot :8080
    |
    v
Temporal :7233
    |
    v
Task Queue: llm-task-queue
    |
    v
Python Worker + Qwen
```

Services defined in `docker-compose.yml`:

- `backend`: Spring Boot API and Temporal Java Worker.
- `jobs`: Python Worker that loads Qwen and runs inference.
- `postgres`: PostgreSQL database for Spring.
- `temporal`: Temporal development server with Web UI.

## Spring Backend

The backend is located in `backend/` and uses:

- Spring Boot `4.0.8`.
- Kotlin `2.2.21`.
- JDK `21`.
- Gradle Kotlin DSL.
- Spring Data JPA.
- Flyway.
- PostgreSQL Driver.
- SpringDoc OpenAPI.
- HTMX Spring Boot.
- Testcontainers for PostgreSQL integration tests.
- Temporal Java SDK `1.33.0`.

The backend contains the Temporal Workflow, the Temporal client, the Java Worker that executes the Workflow, and the REST controller.

## Python Worker

The worker is located in `jobs/` and uses:

- Python `3.14`.
- `uv` for dependency resolution and installation.
- PyTorch.
- Transformers.
- Accelerate.
- Temporal Python SDK (`temporalio`).
- `Qwen/Qwen2.5-0.5B-Instruct` by default.

The worker listens to the `llm-task-queue` Task Queue. It does not use FastAPI, Flask, or any other HTTP framework.

The image installs `build-essential` because Triton may need to compile code during inference. The model is stored in the `qwen-model-cache` Docker volume to avoid downloading it every time the container is recreated.

You can select another model using `QWEN_MODEL`:

```powershell
docker compose run --rm -e QWEN_MODEL=Qwen/Qwen2.5-1.5B-Instruct jobs
```

## Temporal

Temporal runs in development mode through the `temporal` service in Compose:

- Server: `localhost:7233`.
- Web UI: `http://localhost:8233`.
- Persistence: in-memory, suitable for local development.

Within the Docker network, services connect using:

```text
temporal:7233
```

The backend and worker receive this address through `TEMPORAL_ADDRESS`.

## REST Endpoint

The text generation endpoint is:

```http
POST http://localhost:8080/api/llm/generate
Content-Type: application/json
```

Request body:

```json
{
  "prompt": "Explain what Temporal.io is in a few words"
}
```

The response has the following shape:

```json
{
  "text": "..."
}
```

The complete flow is:

1. Spring receives the HTTP request.
2. Spring starts a Temporal Workflow.
3. The Workflow schedules the `generate_qwen_response` Activity on `llm-task-queue`.
4. The Python worker receives the Activity.
5. Qwen generates the response.
6. Temporal returns the result to the Workflow, and Spring responds to the client.

## Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

## Run with Docker Compose

Build and start all services:

```powershell
docker compose up --build
```

Run in the background:

```powershell
docker compose up -d --build
```

Stop the services:

```powershell
docker compose down
```

To also remove PostgreSQL data and the persistent Qwen cache:

```powershell
docker compose down -v
```

The `jobs` service requests an NVIDIA GPU through Compose. To run inference on the GPU, Docker Desktop must have the corresponding NVIDIA support available.

## Dockerfiles

Each component has its own Dockerfile:

- `backend/Dockerfile`: multi-stage build with Gradle/JDK 21 and an Eclipse Temurin JRE 21 runtime.
- `jobs/Dockerfile`: `uv` image with Python 3.14, worker dependencies, and the build support required by Triton.

## Dev Container

The configuration is in `.devcontainer/devcontainer.json` and uses `docker-compose.yml` with `backend` as the primary service:

- Workspace mounted at `/workspaces/SpringTorch`.
- Port 8080 forwarded.
- Remote user `root`.
- PostgreSQL and Temporal available as Compose services.
- `docker-outside-of-docker` feature enabled.

Docker-outside-of-Docker allows Docker commands to run from the Dev Container through the Docker socket. This is required for Testcontainers to create and manage containers during tests.

To open the environment:

1. Install the VS Code Dev Containers extension.
2. Open the project folder.
3. Run `Dev Containers: Rebuild and Reopen in Container`.

## Local Development

Build the backend from `backend/`:

```powershell
cd backend
.\gradlew.bat build
```

Run the tests:

```powershell
cd backend
.\gradlew.bat test
```

Update Python dependencies:

```powershell
cd jobs
uv lock
uv sync
```

The `jobs/uv.lock` file keeps the resolved versions for the Python worker.

## Requirements

- Docker Desktop with Docker Compose.
- NVIDIA support in Docker Desktop to run Qwen on the GPU.
- VS Code and the Dev Containers extension if using the development environment.
- JDK 21 only if building the backend outside Docker.
- `uv` and Python 3.14 only if running the worker outside Docker.
