@echo off
REM YouTube Sentiment Analysis - Integration Test Runner for Windows

echo.
echo ==========================================
echo YouTube Comment Sentiment Analysis
echo Native Java Test Runner (Windows)
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

REM Check if Maven is installed
mvn -v >nul 2>&1
if %ERRORLEVEL% equ 0 (
    set MVN_CMD=mvn
) else if exist "C:\maven\bin\mvn.cmd" (
    echo [OK] Maven detected at C:\maven\bin\mvn.cmd
    set MVN_CMD=C:\maven\bin\mvn.cmd
) else (
    if not exist "backend\mvnw.cmd" (
        echo [X] Maven and Maven wrapper not found.
        pause
        exit /b 1
    )
    set MVN_CMD=.\mvnw.cmd
)

echo [*] Running Native Java Integration Tests...
echo     Dependencies are isolated in backend/.m2/repository
echo.

cd backend
call %MVN_CMD% clean test -Dmaven.repo.local=.m2/repository

if %ERRORLEVEL% neq 0 (
    echo.
    echo [X] TESTS FAILED
    pause
    exit /b 1
)

echo.
echo [OK] ALL TESTS PASSED SUCCESSFULLY
echo.
pause
