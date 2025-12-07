# ⚛️ React-Spring-AI Backend

A **lightweight Spring Boot backend** designed to expose simple, modern AI chat APIs. It is integrated with **OpenAI** using **Spring AI** and `WebClient` for both synchronous and streaming communication.

---

## ✅ What This Backend Does

This project implements core AI service features necessary for a full-stack application:

* **Synchronous Chat API**
    * `POST /api/v1/chat`
    * Accepts a `ChatRequest` and returns a standard `ChatResponse`.

* **Streaming Chat API (SSE)**
    * `POST /api/v1/chat/stream`
    * Returns **Server-Sent Events** (`text/event-stream`) for streaming tokens in real-time.

* **WebClient-based OpenAI Call**
    * The backend directly calls the OpenAI API endpoint:
        ```
        POST [https://api.openai.com/v1/chat/completions](https://api.openai.com/v1/chat/completions)
        ```

* **Basic Rate-Limiting**
    * A custom `RateLimitFilter` is implemented to prevent excessive request frequency.

* **CORS Support**
    * Allows API calls from common React development servers:
        ```
        http://localhost:5173
        http://localhost:3000
        ```

* **Configurable**
    * Model, temperature, tokens, and streaming options are configurable via `application.properties`.

---

## ❌ What This Backend Does NOT Include (Intentional Scope)

These features are **intentionally omitted** to keep the backend simple and focused on the AI integration layer:

* No **JWT authentication**
* No **login/signup** or **role/permission system**
* No **database storage** or **chat history**
* No **device/IP logging**
* No **password reset** or **email service**

---

## 📦 Prerequisites

Ensure you have the following installed to build and run the project:

* **Java 17+**
* **Maven 3.8+**
* **Git**
* **OpenAI API Key**

---

## 🔧 Configuration

The recommended way to provide your **OpenAI API key** is by setting an **environment variable**.

| Operating System | Command |
| :--- | :--- |
| **Linux / macOS** | `export OPENAI_API_KEY="sk-xxxxxxxx"` |
| **Windows PowerShell** | `$env:OPENAI_API_KEY="sk-xxxxxxxx"` |

---

## 🛠 Build & Run

### 1. **Clone Repo**

```bash
git clone [https://github.com/VaibhavsCoding/react-spring-ai-backend.git](https://github.com/VaibhavsCoding/react-spring-ai-backend.git)
cd react-spring-ai-backend
```

### 2. Build
````bash
mvn clean package
````

### 3. Run (Recommended)
   Set the API key and run the Spring Boot application:
````bash
export OPENAI_API_KEY="sk-..."
mvn spring-boot:run
````

### 4. Run JAR
   Alternatively, execute the packaged JAR:
````bash
java -jar target/your-artifact-name.jar
````
The server will be running and accessible at:
````bash
http://localhost:8080
````
### 🔌 API Usage🟦

1.Synchronous ChatThis is a standard blocking API call.

Endpoint POST ````/api/v1/chat````

**RequestBody** (Content-Type: application/json)

`JSON`
````
{
"prompt": "Write a friendly hello message and list three jokes.",
"stream": false
}
````

**Response Body**

`JSON`
````
{
"id": "chatcmpl-abc123",
"text": "Hello! ... (assistant text here)"
}
````

### 🟩 2. Streaming Chat (SSE)

This endpoint returns a stream of tokens as they are generated.

Endpoint POST ````/api/v1/chat/stream````

**Request Body** (Content-Type: application/json)

`JSON`
````
{ "prompt": "Stream a short story." }
````

**Response (`Content-Type: text/event-stream`)** The frontend receives incremental data chunks via an EventSource.


### ⚠️ Troubleshooting (Common Issues)

| Issue | Cause & Fix |
| :--- | :--- |
| **❌ 401 Unauthorized (OpenAI)** | **Cause:** Wrong or missing API key.<br>**Fix:** Ensure the `OPENAI_API_KEY` environment variable is set correctly. |
| **❌ 429 Too Many Requests** | **Cause:** OpenAI rate limit exceeded (or local rate limit triggered).<br>**Fix:** Reduce request frequency or add a delay between calls. |
| **❌ “Endpoint must not be empty”** | **Cause:** Azure OpenAI auto-configuration was triggered.<br>**Fix:** Ensure you have the following line in `application.properties`: `spring.ai.azure.openai.enabled=false` |
| **❌ Placeholder not resolved** | **Cause:** The environment variable was not loaded by Spring.<br>**Fix:** Set the `OPENAI_API_KEY` environment variable correctly *before* running `mvn spring-boot:run`. |
| **❌ Port 8080 already in use** | **Fix 1 (Change Port):** Set `server.port=9090` in `application.properties`.<br>**Fix 2 (Kill Process):** **Windows:** Find PID using `netstat -ano \| findstr :8080` then kill with `taskkill /PID <PID> /F`. |
### 🔭 Recommended Future Enhancements

The following features can be added to evolve the backend into a production-ready application:
1. Security: JWT Authentication, Full User Accounts, Role/Permission System.
2. Data & State: Chat history (DB + JPA), Device/IP tracking, Per-user rate limit.
3. Operations: Admin analytics dashboard, Moderation and validation layer.

### 📄 License
````
MIT License
````