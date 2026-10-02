@echo off
title SmartBank — Localhost Web Server (http://localhost:8080)
echo ========================================================
echo        SMARTBANK — LOCALHOST WEB SERVER
echo ========================================================
echo.

cd /d "%~dp0"

if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.3\jbr" (
    set "JAVA=C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.3\jbr\bin\java.exe"
) else if exist "%USERPROFILE%\.jdks\openjdk-25.0.2" (
    set "JAVA=%USERPROFILE%\.jdks\openjdk-25.0.2\bin\java.exe"
) else (
    set "JAVA=java"
)

set "CP=target\classes;C:\Users\ragul\.m2\repository\com\mysql\mysql-connector-j\9.0.0\mysql-connector-j-9.0.0.jar;C:\Users\ragul\.m2\repository\com\google\protobuf\protobuf-java\4.26.1\protobuf-java-4.26.1.jar"

echo [1/2] Starting Java HTTP Server on http://localhost:8080...
echo [2/2] Opening your browser...
echo.

start http://localhost:8080
"%JAVA%" -cp "%CP%" com.smartbank.WebServer
pause
