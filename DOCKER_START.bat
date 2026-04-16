@echo off
setlocal enabledelayedexpansion

echo.
echo ============================================================
echo      YOUTUBE COMMENT DISCOVERY - DOCKER DEPLOYMENT
echo ============================================================
echo.

REM --- PREREQUISITE CHECKS ---

echo [*] Checking for Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [X] ERROR: Docker is not running or not installed.
    echo     Please start Docker Desktop and try again.
    pause
    exit /b 1
)
echo  [OK] Docker is ready.

echo [*] Checking for Docker Compose...
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [X] ERROR: Docker Compose not found.
    pause
    exit /b 1
)
echo  [OK] Docker Compose is ready.

echo.
echo [*] Orchestrating Container Build and Startup...
echo     (Note: First run may take a few minutes for image downloads)
echo.

REM Move to infra directory and run compose
pushd infra
docker-compose up --build -d
if errorlevel 1 (
    echo.
    echo [X] ERROR: Docker Compose failed to start the containers.
    echo     Check the console output above for details.
    popd
    pause
    exit /b 1
)
popd

echo.
echo ============================================================
echo   🚀 SYSTEM DEPLOYED SUCCESSFULLY
echo ============================================================
echo.
echo   - Frontend Interface: http://localhost:3000
echo   - Backend API:        http://localhost:8080
echo   - Health Dashboard:   http://localhost:3000/health
echo.
echo   Commands:
echo     - View Logs:    docker-compose -f infra/docker-compose.yml logs -f
echo     - Stop System:  docker-compose -f infra/docker-compose.yml down
echo.
echo   Press any key to launch the application in your browser...
pause >nul
start http://localhost:3000
exit
