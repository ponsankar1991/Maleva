<#
  Starts the API on a chosen port with the user-scope environment variables
  (MAIL_*, WIALON_*, MYINVOIS_*, GEMINI_API_KEY, QNE_* ...) loaded fresh from the
  registry, so a `setx` made a minute ago is picked up without restarting the
  terminal, IntelliJ or the Claude app that spawned this process.

  Usage:  powershell -ExecutionPolicy Bypass -File run-local.ps1 [-Port 8083]
#>
param([int]$Port = 8082)

$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$userEnv = Get-ItemProperty -Path 'HKCU:\Environment'
foreach ($p in $userEnv.PSObject.Properties) {
    if ($p.Name -match '^(MAIL_|WIALON_|MYINVOIS_|GEMINI_|LLM_|QNE_|OPENAI_|ANTHROPIC_|FILE_)') {
        Set-Item -Path "Env:$($p.Name)" -Value $p.Value
    }
}
if ([string]::IsNullOrWhiteSpace($env:MAIL_SMTP_HOST)) { Write-Host "Mail sender: NOT configured (MAIL_SMTP_HOST unset)" } else { Write-Host "Mail sender: $env:MAIL_SMTP_HOST" }
Set-Location $here
& "$here\mvnw.cmd" -f "$here\pom.xml" "-Dspring-boot.run.workingDirectory=$here" "-Dspring-boot.run.arguments=--server.port=$Port" spring-boot:run
