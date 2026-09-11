@echo off
rem Builds the deployable artifact and runs it with the production profile
rem (port 8083 - application-prod.yaml), which is what IIS/ARR proxies to.
rem
rem The packaging is <jar>, so `mvn package` produces api-0.0.1-SNAPSHOT.JAR.
rem This script named a .war, which has not existed since the switch, so the
rem java -jar line failed every time and the running server kept serving the
rem previous build.

call mvn clean package -DskipTests
if errorlevel 1 (
  echo.
  echo   BUILD FAILED - not starting. Fix the errors above first.
  pause
  exit /b 1
)

java -jar target\api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
pause
