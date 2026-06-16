## Why

The Maleva API currently lacks comprehensive logging infrastructure. Implementing Logback with hourly rolling log files will enable better observability, debugging, and production monitoring. This is critical for an enterprise ERP system to track transactions, errors, and performance issues in production environments where log volume can be substantial.

## What Changes

- Add Logback as the primary logging framework for the application
- Configure logging levels (DEBUG, INFO, WARN, ERROR) with appropriate appenders
- Implement hourly rolling policy with time-based rollover using the pattern `yyyy-MM-dd_HH`
- Set maximum log file size to 100MB to prevent disk space exhaustion
- Configure history retention to 2400 files (approximately 100 days at hourly rollover)
- Create a `logback-spring.xml` configuration file that replaces default Spring Boot logging configuration
- Configure both console and file appenders for development and production profiles

## Capabilities

### New Capabilities
- `logback-implementation`: Core Logback integration with hourly rolling file appenders, configurable log levels, and file retention policies
- `logback-configuration`: Spring profile-aware Logback configuration (development console logging, production file-based logging with rolling policies)

### Modified Capabilities
<!-- Leave empty if no requirement changes -->

## Impact

- Configuration files: Add `logback-spring.xml` to `src/main/resources/`
- Dependencies: Add Logback dependency to `pom.xml` (included in Spring Boot by default)
- Behavior: Application will now write logs to both console (dev) and rolling files (production)
- Log directory: Logs will be written to `logs/` directory with hourly rollover
- Storage: Maximum 100GB of logs (2400 files × 100MB) before oldest files are deleted

