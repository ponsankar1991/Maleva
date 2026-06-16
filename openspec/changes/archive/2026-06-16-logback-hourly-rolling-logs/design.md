## Context

Currently, the Maleva application uses Spring Boot's default Logback configuration (limited console output). The application lacks structured logging to files, making it difficult to debug production issues, audit transactions, or analyze performance. As an enterprise ERP system, Maleva needs robust log management with size limitations and retention policies to prevent disk space exhaustion in production environments.

## Goals / Non-Goals

**Goals:**
- Implement Logback as the primary logging framework with hourly file rotation
- Configure file rolling policy with 100MB max file size and 2400 file history (≈100 days retention)
- Create profile-specific configurations (development console output, production file-based logging)
- Use time-based rollover pattern `yyyy-MM-dd_HH` for hourly granularity
- Maintain backward compatibility with existing Spring Boot logging configuration mechanisms
- Set up structured logging with appropriate log levels (DEBUG, INFO, WARN, ERROR)

**Non-Goals:**
- Implement log aggregation or centralized logging (ELK, Splunk, etc.)
- Add async logging appenders or performance optimizations beyond Logback defaults
- Implement log filtering based on user roles or data masking
- Change existing logging statements in application code
- Implement metrics or metrics-based alerting

## Decisions

### Decision 1: Use Spring Boot's Native Logback Integration
**Approach:** Leverage Spring Boot's built-in Logback integration with a `logback-spring.xml` configuration file in `src/main/resources/`.

**Rationale:** 
- Spring Boot automatically detects `logback-spring.xml` and uses it instead of `logback.xml`
- Allows Spring profiles (`spring-boot:run`, `dev`, `prod`) to control logging behavior
- Reduces external dependencies; Logback is already included in Spring Boot starter-logging

**Alternatives Considered:**
- Custom logging configuration via `application.yaml`: Less flexible for complex patterns and appender setup
- Third-party logging providers (Log4j2, SLF4J): Adds unnecessary complexity; Logback is the standard

### Decision 2: Hourly Time-Based Rolling with Fixed File Size Cap
**Approach:** Use Logback's `TimeBasedRollingPolicy` with hourly rollover pattern `yyyy-MM-dd_HH` AND a `SizeAndTimeBasedRollingPolicy` to cap individual files at 100MB.

**Rationale:**
- Hourly rollover provides logical separation for operations and debugging
- 100MB cap prevents any single file from becoming unmanageably large
- 2400 file history ≈ 100 days of logs (2400 hours ÷ 24 hours/day), allowing trend analysis
- Automatic deletion of files older than history limit prevents disk space exhaustion

**Alternatives Considered:**
- Daily rollover: Too coarse; large production environments would generate 100MB+ daily files
- Only size-based rolling: No time-based context; harder to correlate with business events
- Size-only with 2400 history: Could result in >240GB logs if not carefully tuned

### Decision 3: Profile-Aware Configuration
**Approach:** Configure two appender strategies:
- **Development (implicit default)**: Console appender with INFO level and detailed pattern
- **Production (profile `prod`)**: File appender with rolling policy, WARN level to reduce noise

**Rationale:**
- Developers need console output during active development
- Production environments should log to files for audit/compliance and to avoid console I/O overhead
- Using Spring profiles keeps configuration clean and environment-specific

**Alternatives Considered:**
- Single configuration for all environments: Impractical; production console logging creates chaos
- Environment variable-based configuration: Valid but less maintainable than Spring profiles

### Decision 4: Log Directory Structure
**Approach:** Store logs in `logs/` directory at application root, with naming convention `application-YYYY-MM-DD_HH.log`.

**Rationale:**
- Predictable location for operations teams
- Follows common Spring Boot conventions
- Easily discoverable for monitoring tools and log aggregators
- Directory structure scales well with hourly rotation

**Alternatives Considered:**
- `/var/log/maleva/`: Requires elevated permissions on Linux; less portable across Windows/Linux
- System temp directory: Unsafe; files could be deleted by OS cleanup processes

### Decision 5: Charset and Pattern
**Approach:** Use UTF-8 charset with comprehensive logging pattern: `[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level %logger{36} - %msg%n`

**Rationale:**
- UTF-8 supports international characters in logs (important for global ERP system)
- Pattern includes timestamp (debugging), thread (concurrency issues), level (severity), logger name (source), message
- 36-character logger truncation reduces clutter while remaining readable

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Disk space exhaustion from log files | Production outage if drive fills | 2400-file history limit + 100MB size cap = ~240GB max. Monitor with alerting. |
| Performance impact of file I/O | Possible latency spikes | Use async appenders in future iteration if needed; acceptable trade-off for now. |
| Log rotation during peak traffic | Potential for lost messages if rotation fails | Logback handles rotation atomically; risk is minimal but can be mitigated with async queues. |
| Developer confusion with profile switching | Logs missing in dev due to wrong profile | Document in README; IDE run configurations should be provided. |
| GDPR/Privacy: logs contain sensitive data | Compliance risk if logs not properly secured | Docs recommend file permission restrictions (`chmod 600 logs/`); consider PII masking in future. |

## Migration Plan

1. **Phase 1 - Configuration (Immediate)**
   - Create `logback-spring.xml` in `src/main/resources/`
   - Update `pom.xml` if needed to ensure Logback is included (already in Spring Boot starter-logging)
   - Add profile-specific logging configuration for `dev` and `prod`

2. **Phase 2 - Testing (Same deployment)**
   - Test on development machine: Verify console output works
   - Test with `prod` profile: Verify file appender and rolling policy
   - Confirm hourly rollover and file naming pattern

3. **Phase 3 - Deployment (No downtime)**
   - Deploy updated JAR to test/staging environment
   - Monitor log file generation and rollover for 24 hours
   - Verify no disk space issues before production

4. **Phase 4 - Production (Rolling deployment)**
   - Deploy to production in stages (no restart required, just config reloading)
   - Verify logs directory permissions and disk space
   - Document log rotation schedule for operations team

**Rollback Strategy:** If issues occur, revert to Spring Boot default logging by removing `logback-spring.xml`. This is reversible without code changes.

## Open Questions

1. Should we include log rotation for specific modules at different levels (e.g., `my.maleva.api.integration.*` at TRACE for debugging)?
2. Should we add per-module log file appenders (e.g., separate files for payment, accounting)?
3. Do we need async logging appenders for high-throughput scenarios?
4. Should logs include correlation IDs for request tracing?

