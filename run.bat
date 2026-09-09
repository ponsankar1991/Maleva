@echo off
rem Local development server (http://localhost:8082).
rem
rem Credentials are NOT set here any more. They used to be, in plain text, in a
rem file committed to git — the Wialon token and the Gemini API key were readable
rem by anyone with repository access and are still in this file's history.
rem
rem They now live in secrets.yaml next to this script, which .gitignore excludes.
rem Copy secrets.yaml.example to secrets.yaml and fill it in once; Spring reads
rem it automatically on every run.
rem
rem To recover the old values: git show HEAD:run.bat
rem Better: reissue them, since they have been exposed.

if not exist "secrets.yaml" (
  echo.
  echo   secrets.yaml not found.
  echo   Run:  copy secrets.yaml.example secrets.yaml
  echo   then fill in the credentials. Continuing without it — integrations
  echo   that need a key will report a clear error at startup.
  echo.
)

set WIALON_ENABLED=true
set WIALON_SYNC_ENABLED=true
set LLM_DEFAULT_PROVIDER=gemini

.\mvnw.cmd spring-boot:run
pause
