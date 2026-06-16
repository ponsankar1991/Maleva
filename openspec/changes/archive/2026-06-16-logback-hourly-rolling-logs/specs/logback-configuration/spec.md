## ADDED Requirements

### Requirement: Profile-Aware Logging Configuration
The application SHALL support different logging configurations based on Spring profiles to optimize behavior for development and production environments.

#### Scenario: Default/development profile uses console appender
- **WHEN** the application runs with no Spring profile specified or with `dev` profile
- **THEN** logs are written to the console (stdout) with appropriate formatting for developer readability

#### Scenario: Production profile uses file appender
- **WHEN** the application runs with `prod` profile
- **THEN** logs are written to rolling files in the `logs/` directory, not to console

### Requirement: Development Console Logging Configuration
In development mode, the application SHALL output logs to the console with INFO level and a detailed format suitable for interactive debugging.

#### Scenario: Console output in development
- **WHEN** a developer runs the application from IDE or with `./mvnw spring-boot:run`
- **THEN** logs appear in the console with full details (timestamp, thread, level, logger, message)

#### Scenario: INFO level filtering in development
- **WHEN** application runs in development mode
- **THEN** logs at INFO level and above (WARN, ERROR) are displayed; DEBUG is not shown by default

#### Scenario: Easy log filtering for specific modules
- **WHEN** developer wants to debug a specific module (e.g., payment processing)
- **THEN** the configuration supports setting package-level logging via environment variables or `application.yaml` overrides

### Requirement: Production File Logging Configuration
In production mode, the application SHALL output logs to files only (not console) with WARN level to reduce noise and focus on errors and warnings.

#### Scenario: No console output in production
- **WHEN** the application runs with `prod` profile in production environment
- **THEN** logs are NOT written to console, only to files

#### Scenario: WARN level filtering in production
- **WHEN** application runs in production mode
- **WHEN** a DEBUG or INFO level message is logged
- **THEN** the message is NOT written to log files

#### Scenario: WARN and ERROR messages are preserved
- **WHEN** application runs in production mode
- **WHEN** a WARN or ERROR level message is logged
- **THEN** the message is written to the rolling log file

### Requirement: Configuration File Location
The Logback configuration SHALL be stored in `logback-spring.xml` in the `src/main/resources/` directory, allowing Spring Boot to automatically detect and apply it.

#### Scenario: logback-spring.xml is auto-detected
- **WHEN** the application starts
- **THEN** Spring Boot automatically finds and loads `src/main/resources/logback-spring.xml`

#### Scenario: Configuration overrides Spring Boot defaults
- **WHEN** `logback-spring.xml` is present
- **THEN** Spring Boot's default logging configuration is ignored and logback-spring.xml controls all logging behavior

### Requirement: UTF-8 Charset for Log Files
All log files SHALL be written using UTF-8 charset to support international characters in log messages.

#### Scenario: International characters in logs
- **WHEN** a log message contains non-ASCII characters (e.g., customer names in Arabic, Chinese, Cyrillic)
- **THEN** the characters are correctly encoded and readable in log files

#### Scenario: Proper encoding across platforms
- **WHEN** log files are viewed on different platforms (Linux, Windows, macOS)
- **THEN** UTF-8 encoding ensures consistent character representation across all platforms

