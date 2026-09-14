@echo off
setlocal
set GRADLE_VERSION=8.7
set BASE_DIR=%~dp0
set DIST_DIR=%BASE_DIR%.gradle-dist
set GRADLE_HOME=%DIST_DIR%\gradle-%GRADLE_VERSION%
set ZIP_FILE=%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP_FILE%'; Expand-Archive -Force '%ZIP_FILE%' '%DIST_DIR%'"
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
