## ADDED Requirements

### Requirement: Logback Framework Integration
The application SHALL use Logback as the primary logging framework with automatic Spring Boot integration via `logback-spring.xml` configuration file.

#### Scenario: Application initializes with Logback
- **WHEN** the application starts up
- **THEN** Logback is configured as the logging framework for all application modules

#### Scenario: SLF4J APIs use Logback implementation
- **WHEN** application code uses SLF4J Logger APIs (LoggerFactory.getLogger)
- **THEN** SLF4J delegates to Logback as the underlying implementation

### Requirement: Hourly Rolling File Appender
The application SHALL write logs to files that automatically roll over every hour, creating a new log file with the name pattern `application-YYYY-MM-DD_HH.log`.

#### Scenario: Log file rolls at hour boundary
- **WHEN** the hour changes (e.g., from 14:59 to 15:00)
- **THEN** the current log file is closed and a new file with the new hour is created (e.g., `application-2026-06-16_15.log`)

#### Scenario: Filename reflects creation time
- **WHEN** a log file is created at 3:45 PM
- **THEN** the log file is named `application-YYYY-MM-DD_15.log` (reflecting the hour, not minutes)

### Requirement: Maximum File Size Cap
The application SHALL not allow individual log files to exceed 100MB in size. If a file reaches 100MB before the hourly rollover, a new file SHALL be created immediately.

#### Scenario: File reaches 100MB before hour boundary
- **WHEN** a log file grows to 100MB before the next hourly rollover
- **THEN** the system immediately creates a new log file with a numeric suffix (e.g., `application-2026-06-16_14.0.log`, `application-2026-06-16_14.1.log`)

#### Scenario: Multiple files in single hour
- **WHEN** high-volume logging causes multiple 100MB files in a single hour
- **THEN** each file is created with incremental suffix and timestamped appropriately

### Requirement: Log History Retention
The application SHALL retain a maximum of 2400 log files. Older log files SHALL be automatically deleted.

#### Scenario: History limit prevents unbounded growth
- **WHEN** the total number of log files exceeds 2400
- **THEN** the oldest log files are automatically deleted to maintain the limit

#### Scenario: Approximately 100 days of logs retained
- **WHEN** logs roll over hourly at 2400 file history limit
- **THEN** approximately 100 days of logs are retained (2400 hours ÷ 24 hours/day = 100 days) before automatic deletion

### Requirement: Logging Output Directory
The application SHALL write all log files to a `logs/` directory located at the application root, where the application process is started.

#### Scenario: Logs directory is created automatically
- **WHEN** the application starts and logs directory does not exist
- **THEN** the `logs/` directory is created automatically

#### Scenario: Log files are written to logs directory
- **WHEN** the application writes log entries
- **THEN** all log files are written to `logs/application-*.log`

### Requirement: Log Message Format
All log messages SHALL follow a consistent pattern containing: timestamp, thread name, log level, logger name (truncated to 36 characters), and message.

#### Scenario: Log contains required fields
- **WHEN** a log entry is written (e.g., logger.info("User login successful"))
- **THEN** the log line contains: timestamp (yyyy-MM-dd HH:mm:ss.SSS), thread name, level (INFO), logger name, and message
- **EXAMPLE**: `[2026-06-16 14:30:45.123] [http-nio-8082-exec-1] INFO  m.m.a.m.c.UserController - User login successful`

#### Scenario: Logger names are truncated for readability
- **WHEN** a logger name exceeds 36 characters (e.g., `my.maleva.api.module.customer.controller.CustomerController`)
- **THEN** the logger name is truncated to 36 characters from the right to preserve module context

