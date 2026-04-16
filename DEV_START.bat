@echo off
setlocal enabledelayedexpansion

echo.
echo ============================================================
echo      YOUTUBE COMMENT DISCOVERY - FULL SYSTEM START
echo ============================================================
echo.
echo This script will:
echo 1. Set up an isolated Java environment (Maven + Dependencies)
echo 2. Launch the Spring Boot Backend (Port 8080)
echo 3. Launch the Frontend via Python (Port 3000)
echo.

REM --- PREREQUISITE CHECKS ---

echo [*] Checking Prerequisites...

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [X] ERROR: Java 17+ is required but not found.
    pause
    exit /b 1
)
echo  [OK] Java detected.

REM Check Python (for Frontend)
python --version >nul 2>&1
if errorlevel 1 (
    echo [X] WARNING: Python not found. Frontend will not be auto-started.
    set START_FRONTEND=false
) else (
    echo  [OK] Python detected.
    set START_FRONTEND=true
)

echo.
echo [*] Initializing Isolated Dependency Download...
echo     (This may take a few minutes on the first run)
echo.

REM --- BACKEND START ---

REM Load environment variables from infra/.env
if exist "infra\.env" (
    echo [*] Loading environment variables from infra/.env...
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("infra\.env") do (
        set "%%A=%%B"
    )
)

echo [*] Starting Backend in a new window...
start "YouTube Comment Discovery (Backend)" cmd /k ".\scripts\build-and-run.bat"

REM --- FRONTEND START ---

if "%START_FRONTEND%"=="true" (
    echo [*] Starting Frontend on http://localhost:3000...
    start "YouTube Sentiment UI (Frontend)" cmd /c "cd frontend && python -m http.server 3000"
) else (
    echo [!] Please manually open frontend/index.html in your browser.
)

echo.
echo ============================================================
echo   SYSTEM INITIALIZED
echo ============================================================
echo.
echo   - Backend URL:  http://localhost:8080
echo   - Frontend URL: http://localhost:3000 (if Python available)
echo   - Logs:         Check the newly opened terminal windows.
echo.
echo   Press any key to exit this launcher...
pause >nul
