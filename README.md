# DevPilot

**An AI-powered codebase assistant that helps developers understand GitHub repositories using Retrieval-Augmented Generation (RAG).**

DevPilot indexes repository source code and uses relevant code context to answer developers' questions through an AI-powered chat interface.

## Features

- GitHub OAuth authentication
- Repository selection and source-code indexing
- Semantic code retrieval using vector embeddings
- AI-powered codebase question answering
- Repository-specific chat sessions
- Code citations where available

## Tech Stack

- **Frontend:** Next.js, React, TypeScript
- **Backend:** Java 21, Spring Boot
- **AI:** Spring AI, Google Gemini
- **Database:** PostgreSQL, pgvector
- **Authentication:** GitHub OAuth 2.0
- **Infrastructure:** Docker Compose

## Architecture

DevPilot follows a Retrieval-Augmented Generation (RAG) workflow.

```text id="t6grpf"
GitHub Repository
       |
       v
Fetch and Filter Source Files
       |
       v
Split Code into Chunks
       |
       v
Generate Embeddings
       |
       v
PostgreSQL + pgvector
       |
       v
User Asks a Question
       |
       v
Retrieve Relevant Code
       |
       v
Gemini Generates an Answer
       |
       v
Answer + Available Citations
```

The backend coordinates repository access, indexing, retrieval, and AI responses. PostgreSQL stores application data and vector embeddings, while the Next.js frontend provides the user interface.

## Prerequisites

Install:

- Git
- Java JDK 21
- Node.js compatible with the project's Next.js version
- Docker Desktop (Windows/macOS) or Docker Engine with the Compose plugin (Linux)
- Google AI Studio API key
- GitHub OAuth App credentials

Verify your installations:

```bash id="4mkz9a"
git --version
java -version
node --version
npm --version
docker compose version
```

## Setup and Run

### 1. Clone the repository

```bash id="uyyqks"
git clone https://github.com/thrinesh01/Devpilot.git
cd Devpilot
```

### 2. Start PostgreSQL

```bash id="7l54xk"
docker compose up -d postgres
docker compose ps
```

The current local configuration uses database `devpilot`, username `postgres`, and host port `5433`. Check `docker-compose.yml` if your setup differs.

### 3. Configure environment variables

Provide the following values to the backend process:

- `GEMINI_API_KEY`
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`
- `TOKEN_ENCRYPTOR_PASSWORD`
- `TOKEN_ENCRYPTOR_SALT`

Optional database and frontend settings are documented in `backend/backend/src/main/resources/application.properties`.

For local development, configure your GitHub OAuth callback URL as:

```text id="mfbjbp"
http://localhost:8080/login/oauth2/code/github
```

Never put actual credentials in this README or commit populated environment files.

### 4. Start the backend

**Windows PowerShell:**

```powershell id="tct6vc"
cd backend\backend
.\mvnw.cmd spring-boot:run
```

**macOS/Linux:**

```bash id="ujtzq8"
cd backend/backend
chmod +x mvnw
./mvnw spring-boot:run
```

The backend uses port `8080` by default.

### 5. Start the frontend

Open a second terminal from the project root:

```bash id="xx5n7b"
cd client
npm ci
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

Ensure the frontend API URL points to the running backend at `http://localhost:8080`.

### 6. Use DevPilot

1. Sign in with GitHub.
2. Select a repository you can access.
3. Start indexing and wait for it to complete.
4. Open the repository chat.
5. Ask questions about the indexed codebase.

## Troubleshooting

| Issue | What to check |
|---|---|
| PostgreSQL connection refused | Docker status, port `5433`, and database settings |
| `expected 1536 dimensions, not 3072` | Embedding output dimensions and pgvector schema must match |
| `release version 5 not supported` | JDK 21 and Maven compiler configuration |
| `Can't resolve 'shadcn/tailwind.css'` | CSS imports and installed frontend dependencies |
| Database insert fails because ID has no default | Entity ID-generation strategy and database schema |
| GitHub login fails | OAuth credentials and callback URL |

These are known development issues; the troubleshooting table lists checks rather than claiming every issue is already fixed.

## Security

- Never commit API keys, OAuth secrets, or encryption credentials.
- Use non-default credentials and secure configuration in production.
- Keep encryption settings stable when existing encrypted data depends on them.

## License

No license has been specified yet. All rights remain subject to applicable copyright law unless a license is added.
