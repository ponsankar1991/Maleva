## 1. Create Logback Configuration File

- [x] 1.1 Create `src/main/resources/logback-spring.xml` with base configuration
- [x] 1.2 Configure console appender with detailed log pattern `[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level %logger{36} - %msg%n`
- [x] 1.3 Configure file appender with `logs/` directory and UTF-8 charset
- [x] 1.4 Implement hourly rolling policy using `TimeBasedRollingPolicy` with pattern `yyyy-MM-dd_HH`
- [x] 1.5 Add size-based rolling policy with 100MB max file size
- [x] 1.6 Set max file history to 2400 files for retention (≈100 days)
- [x] 1.7 Configure root logger with default INFO level

## 2. Configure Spring Profiles

- [x] 2.1 Create development profile configuration that uses console appender (default/no profile)
- [x] 2.2 Set development profile logging level to INFO
- [x] 2.3 Create production profile configuration that uses file appender only
- [x] 2.4 Set production profile logging level to WARN
- [x] 2.5 Add Spring conditional logic (`<springProfile>`) to enable/disable appenders per profile
- [x] 2.6 Ensure file appender is disabled in development mode (console only)

## 3. Verify Dependencies

- [x] 3.1 Verify Logback is included in Spring Boot starter-logging in pom.xml (should be default)
- [x] 3.2 Verify SLF4J is available as logging facade
- [x] 3.3 Check that no conflicting logging libraries exist (Log4j2, commons-logging)
- [x] 3.4 Verify pom.xml includes spring-boot-starter-logging or equivalent

## 4. Create Logs Directory and Permissions

- [x] 4.1 Create `logs/` directory at project root (or let Logback create it on first run)
- [x] 4.2 Verify logs directory is created automatically when application starts
- [x] 4.3 Test that application has write permissions to logs directory

## 5. Test Development Mode

- [x] 5.1 Run application with `./mvnw spring-boot:run` (implicit dev profile)
- [x] 5.2 Verify logs appear in console with full detail (timestamp, thread, level, logger, message)
- [x] 5.3 Verify console shows INFO and above (no DEBUG by default)
- [x] 5.4 Verify no log files are created in development mode
- [x] 5.5 Generate test log entries at DEBUG, INFO, WARN, ERROR levels and verify filtering

## 6. Test Production Mode

- [x] 6.1 Build application with `./mvnw clean package`
- [x] 6.2 Run JAR with production profile: `java -jar target/maleva-*.jar --spring.profiles.active=prod`
- [x] 6.3 Verify no output appears in console
- [x] 6.4 Verify log files are created in `logs/` directory
- [x] 6.5 Verify log messages use correct format: `[YYYY-MM-DD HH:mm:ss.SSS] [thread] LEVEL logger - message`

## 7. Test Hourly Rolling

- [x] 7.1 Generate log entries and monitor `logs/` directory
- [x] 7.2 Verify log files have naming pattern `application-YYYY-MM-DD_HH.log`
- [x] 7.3 Simulate high-volume logging to trigger size-based rolling (>100MB)
- [x] 7.4 Verify size-based files get numeric suffixes: `application-YYYY-MM-DD_HH.N.log`
- [x] 7.5 Wait for or simulate hour boundary change to verify hourly rollover
- [x] 7.6 Confirm old files are retained up to 2400 count limit

## 8. Test File Retention Policy

- [x] 8.1 Verify 2400 file history limit is configured in `logback-spring.xml`
- [x] 8.2 Generate enough log files to approach or exceed 2400 file limit
- [x] 8.3 Verify oldest files are automatically deleted when limit is exceeded
- [x] 8.4 Calculate and confirm approximately 100 days of logs are retained (2400 hours ÷ 24)

## 9. Test UTF-8 Encoding

- [x] 9.1 Add log entries containing non-ASCII characters (Arabic, Chinese, Cyrillic, emoji)
- [x] 9.2 Verify characters are correctly written to log files with UTF-8 encoding
- [x] 9.3 Read log files on different platforms (if available) and verify character consistency

## 10. Integration Testing

- [x] 10.1 Run existing unit tests to ensure logging doesn't break any tests
- [x] 10.2 Verify H2 in-memory database tests still work with Logback
- [x] 10.3 Run full integration test suite with Logback enabled
- [x] 10.4 Verify no performance regressions from logging implementation

## 11. Documentation and Cleanup

- [x] 11.1 Update `README.md` with instructions for viewing logs (location: `logs/` directory)
- [x] 11.2 Document log rotation schedule: hourly with 100MB cap, 2400 files (≈100 days) retention
- [x] 11.3 Add logging configuration section to `docs/CODING_STANDARDS.md` with best practices
- [x] 11.4 Document how to enable DEBUG logging for specific modules (e.g., via environment variables)
- [x] 11.5 Remove any old/temporary test files created during development
- [x] 11.6 Add `.gitignore` entry for `logs/` directory to prevent committing log files

## 12. Final Validation

- [x] 12.1 Run full `./mvnw clean install` build and verify no compiler errors
- [x] 12.2 Run application in both dev and prod profiles and verify correct logging behavior
- [x] 12.3 Verify all log files are properly formatted and readable
- [x] 12.4 Confirm implementation matches all requirements in specs
- [x] 12.5 Verify disk space usage is reasonable (100GB max with 2400 × 100MB files)
- [x] 12.6 Create git commit with message: "feat: implement Logback with hourly rolling log files"








