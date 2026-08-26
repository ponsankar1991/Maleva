@echo off
rem Wialon GPS integration (token taken from the legacy Common/gpsapilist.cs account)
set WIALON_ENABLED=true
set WIALON_TOKEN=bcf761fd35a8ddcb42c042d48c8bcb95702666AF9C6669DBEA5935A50FD26A59D7E9C8CE
set WIALON_SYNC_ENABLED=true

.\mvnw.cmd spring-boot:run
rem java -jar target\api-0.0.1-SNAPSHOT.war
pause
