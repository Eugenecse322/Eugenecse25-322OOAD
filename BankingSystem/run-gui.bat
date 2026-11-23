@echo off
setlocal
cd /d "%~dp0"

if not exist "lib\javafx\javafx-controls-25-win.jar" (
  echo JavaFX libraries are missing. Downloading...
  powershell -ExecutionPolicy Bypass -File "%~dp0setup-javafx.ps1"
  if errorlevel 1 exit /b 1
)

if not exist out mkdir out
copy /Y src\bank.css out\bank.css >nul
copy /Y src\schema.sql out\schema.sql >nul

echo Compiling...
javac --module-path lib\javafx --add-modules javafx.controls -d out src\*.java
if errorlevel 1 (
  echo Compile failed.
  exit /b 1
)

echo Starting Pula Bank...
java --module-path lib\javafx --add-modules javafx.controls --enable-native-access=javafx.graphics -cp "out;lib\h2-2.3.232.jar" BankingFxApp
