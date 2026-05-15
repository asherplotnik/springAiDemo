# AGENTS.md

## Cursor Cloud specific instructions

### Overview
Spring Boot 4.0.6 banking AI agent demo using Spring AI with Google Gemini. Java 21 required. Serves a Hebrew chat UI on port 8080 and exposes `POST /api/banking-agent-demo/a2a/execute`.

### Dependencies
- **bankingAgentDemo-api** must be running on port 3000 first (tool base URLs default to `http://localhost:3000`).
- A Google GenAI API key is required for chat to work.

### Configuration
- `application.yaml` is git-ignored and must be created in `src/main/resources/`. Required properties:
  - `spring.ai.google.genai.api-key`
  - `spring.ai.google.genai.base-url` (e.g. `https://generativelanguage.googleapis.com`)
  - `spring.ai.google.genai.chat.options.model` (e.g. `gemini-2.5-flash`)
- Tool base URLs default to `http://localhost:3000` if not explicitly set.

### Building and Running
- `./mvnw compile` — compile
- `./mvnw test` — run tests (context-load test; needs `application.yaml` with placeholder values at minimum)
- `./mvnw spring-boot:run` — run the application
- Swagger UI available at `http://localhost:8080/swagger-ui/index.html`

### Gotchas
- `src/main/resources/static/jwt.txt` is git-ignored and serves the UI JWT token. Without it, the chat UI loads but tool calls using auth context may fail.
- The Maven wrapper (`./mvnw`) downloads Maven 3.9.14 on first run.
