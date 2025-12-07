React-Spring-AI Backend

Lightweight Spring Boot backend that exposes simple AI chat APIs backed by OpenAI (via WebClient).
This README documents what the project actually implements today, how to configure and run it, and troubleshooting tips based on your logs.

✅ What this backend does (current functionality)

Provides an HTTP REST endpoint for synchronous chat completions:

POST /api/v1/chat — accepts a ChatRequest and returns a ChatResponse.

Provides an SSE streaming endpoint for streaming completions (if streaming is enabled):

POST /api/v1/chat/stream — produces text/event-stream Server-Sent Events tokens.

Uses a ChatService that calls OpenAI POST https://api.openai.com/v1/chat/completions via WebClient.

Basic rate limiting filter is present (logs show RateLimitFilter in the chain).

CORS configuration to allow your React dev server (e.g. http://localhost:5173 / http://localhost:3000) — configurable via properties.

Uses Spring Boot devtools (restart enabled in dev).

Configurable model, temperature, max tokens and stream toggle via application.properties.

❌ What this backend does not include (so you or your team should add if needed)

I intentionally list these so README consumers don’t assume features that aren’t present.

No user authentication (no JWT, no login/signup endpoints).

No user/account management.

No chat history persistence or DB-backed message storage.

No device/IP logging or login history.

No password reset flows or email sending built-in.

No advanced role/permission support.

If you want any of the above, I can add implementation guidance or code snippets.

📦 Prerequisites

Java 17 (or later).

Maven (3.8+ recommended).

Git (for cloning/pushing repo).

An OpenAI API key (or equivalent) to call OpenAI endpoints.

🔧 Configuration 

Important: In your code you used placeholders like ${OPENAI_API_KEY} — set the environment variable OPENAI_API_KEY before launching the app, or replace with the secret (not recommended for repo).

Example on Windows PowerShell:

$env:OPENAI_API_KEY="sk-xxxxxxxx..."
mvn spring-boot:run


Linux / macOS:

export OPENAI_API_KEY="sk-xxxxxxxx..."
mvn spring-boot:run

🛠 Build & Run

Clone repo:

git clone https://github.com/VaibhavsCoding/react-spring-ai-backend.git
cd react-spring-ai-backend


Build:

mvn clean package


Run:

# Use environment var for API key (recommended)
export OPENAI_API_KEY="sk-..."
mvn spring-boot:run


Or run the packaged jar:

java -jar target/your-artifact-name.jar


Server listens by default on http://localhost:8080

🔌 API — request/response shapes & examples
Synchronous chat (simple)

Request

POST /api/v1/chat
Content-Type: application/json

{
  "prompt": "Write a friendly hello message and list three jokes.",
  "stream": false
}


Response (successful)

{
  "id": "chatcmpl-abc123",
  "text": "Hello! ... (assistant text here)"
}


Implementation note: ChatRequest accepts either a prompt string or a messages list (your ChatService.prepareMessages supports both). The controller returns a ChatResponse object with id and text.

Streaming via SSE (if spring.ai.openai.chat.options.stream=true)

Request

POST /api/v1/chat/stream
Content-Type: application/json

{ "prompt": "Stream a short story." }


Response
The endpoint returns Content-Type: text/event-stream and sends small chunks (SSE data: lines) — your frontend can connect with EventSource.

⚠️ Troubleshooting & common errors (from logs you shared)

401 Unauthorized from OpenAI:

Cause: invalid/expired API key or key incorrectly supplied to OpenAI requests.

Fix: ensure OPENAI_API_KEY env var is set and ChatService/WebClient is passing Authorization: Bearer <key> (your code does that when you set property correctly).

429 Too Many Requests from OpenAI:

Cause: API quota/rate limit reached.

Fix: reduce request frequency / backoff retries / switch model or account.

Endpoint must not be empty / Azure OpenAI bean errors:

Cause: Spring AI auto-config attempted to configure Azure OpenAI because Azure config beans were on the classpath but no Azure endpoint was supplied.

Fix: disable Azure auto-config (you already added spring.ai.azure.openai.enabled=false) — ensure property is present and effective.

Could not resolve placeholder 'spring.ai.openai.api-key':

Fix: set the OPENAI_API_KEY environment variable or put the API key into application.properties (avoid committing secrets).

Port 8080 was already in use:

Fix: either stop the other process using 8080 (Windows: netstat -ano/taskkill) or change server.port in application.properties.

🔭 Development notes & next steps (suggestions)

If you'd like to turn this into a full product-ready backend, consider adding:

Authentication (JWT) + user management

Chat history persistence (DB + JPA)

Rate-limit per user

API keys / quota for your frontend users

Admin UI or metrics (Prometheus/Grafana)

Robust retry/backoff for OpenAI calls

Input sanitization & moderation checks

I can scaffold any of these for you.

📄 License

Add your preferred license (MIT / Apache / etc.). Example:

MIT License
