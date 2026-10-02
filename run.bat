@echo off
title SmartBank - Bank Management System
echo ========================================================
echo         SMARTBANK - BANK MANAGEMENT SYSTEM
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

set "MP=C:\Users\ragul\.m2\repository\org\openjfx\javafx-base\21.0.5\javafx-base-21.0.5-win.jar;C:\Users\ragul\.m2\repository\org\openjfx\javafx-controls\21.0.5\javafx-controls-21.0.5-win.jar;C:\Users\ragul\.m2\repository\org\openjfx\javafx-graphics\21.0.5\javafx-graphics-21.0.5-win.jar"
set "CP=target\classes;C:\Users\ragul\.m2\repository\com\mysql\mysql-connector-j\9.0.0\mysql-connector-j-9.0.0.jar;C:\Users\ragul\.m2\repository\com\google\protobuf\protobuf-java\4.26.1\protobuf-java-4.26.1.jar"

echo Starting SmartBank Application...
echo.

"%JAVA%" --module-path "%MP%" --add-modules javafx.controls,javafx.base,javafx.graphics -cp "%CP%" com.smartbank.Main

echo.
echo Application closed.
pause
