# YouTube Sentiment Discovery - Operations Guide

This guide covers the setup, deployment, and maintenance of the YouTube comment discovery platform.

---

## 🛠️ Environment Setup

### 1. YouTube Data API Key
1.  Visit the [Google Cloud Console](https://console.cloud.google.com).
2.  Enable the **YouTube Data API v3**.
3.  Generate an **API Key** under Credentials.
4.  **Local Configuration**: 
    - Create a file at `infra/.env`.
    - Add: `YOUTUBE_API_KEY=your_key_here`.

---

## 🚀 Execution Workflows

The project provides two primary startup methods designed for ease of use.

### A. Docker Deployment (Standard)
Best for testing the full production-like environment with Nginx proxying.
1.  **Launch**: Double-click `DOCKER_START.bat` in the root folder.
2.  **Verify**: Access [http://localhost:3000](http://localhost:3000).
3.  **Monitor**: Run `docker-compose -f infra/docker-compose.yml logs -f` to see real-time output.

### B. Development Startup (Native)
Best for making code changes and immediate testing.
1.  **Launch**: Double-click `DEV_START.bat`.
2.  **Prerequisites**: Requires Java 17 (JDK) and Python 3.x installed on your PATH.

---

## 🏗️ Architecture & Isolation

- **Isolated Maven**: To prevent local environment pollution, the project uses a localized `.m2` repository during native builds.
- **Microservice Layout**:
    - **Backend**: Spring Boot 3.x service on port `8080`.
    - **Frontend**: Static Nginx/JS application on port `3000`.

---

## 🧪 Testing & Validation

### 1. Automated Integration Suite
We use a robust JUnit 5 and Spring Boot Test suite to validate the extraction logic.
```bash
# On Windows root
.\scripts\run-tests.bat
```

### 2. Manual Diagnostics
- **Backend Health**: `GET http://localhost:8080/api/sentiment/health`
- **Frontend Health**: `GET http://localhost:3000/health` (Served by Nginx)

---

## 🚨 Troubleshooting

- **Docker Build Fails**: Ensure Docker Desktop is running. If you get an `openjdk` not found error, ensure you have updated to the `eclipse-temurin` images.
- **Zero Comments Extracted**: Check if the video has comments disabled or if your API Key has exceeded its daily quota.
