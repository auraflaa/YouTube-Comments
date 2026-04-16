@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven2 Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'
@echo off
@REM set title of command window
title %0
@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%USERPROFILE%")

@REM Execute a user defined script before this one
if not "%MAVEN_SKIP_RC%" == "on" (
  if exist "%HOME%\mavenrc_pre.bat" call "%HOME%\mavenrc_pre.bat"
)

setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@REM Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~f i

@REM Double quotes in case of space in path
set "WRAPPER_JAR=%APP_HOME%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%APP_HOME%\.mvn\wrapper\maven-wrapper.properties"
set "MW_VERSION=3.1.0"

@REM Download wrapper jar if not exists
if not exist "%WRAPPER_JAR%" (
    echo [INFO] Downloading Maven Wrapper JAR...
    curl.exe -sL -o "%WRAPPER_JAR%" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/%MW_VERSION%/maven-wrapper-%MW_VERSION%.jar"
)

@REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
@REM Fallback to current working directory if not found.

set "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"
if not "%MAVEN_PROJECTBASEDIR%" == "" goto endReadBaseDir

set "MAVEN_PROJECTBASEDIR=%APP_HOME%"
:findBaseDir
if exist "%MAVEN_PROJECTBASEDIR%\.mvn" goto endReadBaseDir
set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%\.."
if "%MAVEN_PROJECTBASEDIR%" == ".." goto fallbackBaseDir
goto findBaseDir

:fallbackBaseDir
set "MAVEN_PROJECTBASEDIR=%APP_HOME%"

:endReadBaseDir

IF NOT "%JAVA_HOME%" == "" goto haveJavaHome

for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
if not "%JAVACMD%" == "" goto setupArgs

echo.
echo Error: JAVA_HOME is not defined correctly.
echo   We cannot execute %JAVACMD%
echo.
goto fail

:haveJavaHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:setupArgs
set "CLASSPATH=%WRAPPER_JAR%"

@REM Start Maven
"%JAVACMD%" %MAVEN_OPTS% -classpath "%CLASSPATH%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*

:fail
exit /B 1

:end
if "%MAVEN_BATCH_PAUSE%" == "on" pause

if "%MAVEN_TERMINATE_CMD%" == "on" exit %ERRORLEVEL%

exit /B %ERRORLEVEL%
