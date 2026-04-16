@echo off
REM YouTube Sentiment Analysis - Build and Run Script for Windows

echo.
echo ==========================================
echo YouTube Comment Sentiment Analysis
echo Build and Run Script (Windows)
echo ==========================================
echo.

REM Change to project root (parent directory of 'scripts')
cd /d "%~dp0.."

REM Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [X] Java is not installed. Please install Java 17 or higher.
    pause
    exit /b 1
)

echo [OK] Java is installed

REM Check if Maven is installed
call mvn -v >nul 2>&1
if %ERRORLEVEL% equ 0 (
    set MVN_CMD=mvn
) else if exist "C:\maven\bin\mvn.cmd" (
    echo [OK] Maven detected at C:\maven\bin\mvn.cmd
    set MVN_CMD=C:\maven\bin\mvn.cmd
) else (
    echo [!] Maven not found. Checking for maven wrapper...
    if not exist "backend\mvnw.cmd" (
        echo [X] Maven and Maven wrapper not found.
        pause
        exit /b 1
    )
    set MVN_CMD=.\mvnw.cmd
)

echo [OK] Using Maven Command: %MVN_CMD%
echo.

echo [*] Building backend... (This may take a moment)
cd backend
call %MVN_CMD% clean install -DskipTests "-Dmaven.repo.local=../.m2/repository" -q

if %ERRORLEVEL% neq 0 (
    echo [X] Backend build failed. Error code: %ERRORLEVEL%
    pause
    exit /b 1
)

echo [OK] Backend build successful
echo.

echo [*] Starting backend server...
echo     Backend will run on http://localhost:8080
echo     Press Ctrl+C to stop
echo.

call %MVN_CMD% spring-boot:run "-Dmaven.repo.local=../.m2/repository"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [!] Backend server stopped unexpectedly.
    pause
)
