# JWT Authentication Filter - Enhanced Logging & MDC

## Overview

The `JwtAuthenticationFilter` has been enhanced with comprehensive request/response logging and **MDC (Mapped Diagnostic Context)** support for better observability and debugging.

## Features Added

### 1. **Request ID Correlation (MDC)**
- Automatically generates a unique request ID for each request
- Reads `X-Request-ID` header if provided; otherwise generates a UUID
- Stores in MDC as `requestId` - appears in all logs for this request
- Enables correlation of logs across distributed systems

### 2. **Request Logging**
Logs incoming request details including:
- HTTP method (GET, POST, etc.)
- Full endpoint path (e.g., `/api/v1/customers/123`)
- Client IP address (with proxy header support)
- Query string parameters
- Request ID for correlation

**Sample Log Output:**
```
[2026-06-16 14:32:15.234] [http-nio-8082-exec-5] INFO  - Incoming request - Method: POST, Endpoint: POST /api/v1/customers, Client-IP: 192.168.1.100, Query: none, Request-ID: 7f8c3e4d-6b2a-11ec-8000-0242ac1a0001
requestId=7f8c3e4d-6b2a-11ec-8000-0242ac1a0001 | username=john.doe | endpoint=POST /api/v1/customers | 
```

### 3. **Response Logging**
Logs response details including:
- Endpoint that was called
- HTTP status code
- Status category (Success, Redirect, Client Error, Server Error)
- Request ID for correlation

**Sample Log Output:**
```
[2026-06-16 14:32:15.456] [http-nio-8082-exec-5] INFO  - Response sent - Endpoint: POST /api/v1/customers, Status: 201 (Success), Request-ID: 7f8c3e4d-6b2a-11ec-8000-0242ac1a0001
```

### 4. **JWT Authentication Logging**
Enhanced JWT processing logs:
- Successful token validation with user and roles
- Token validation failures
- Missing or malformed Authorization header
- Any exceptions during JWT processing

**Sample Log Output:**
```
[2026-06-16 14:32:15.250] [http-nio-8082-exec-5] INFO  - JWT token validated successfully for user: john.doe, roles: [ROLE_ADMIN]
```

### 5. **MDC Context Variables**
Three MDC variables are automatically set for each request:
- **`requestId`** - Unique identifier for request correlation
- **`username`** - Authenticated user (set only if JWT validation succeeds)
- **`endpoint`** - HTTP method + URI (e.g., "GET /api/v1/customers")

These variables will appear in all logs generated during request processing, enabling you to:
- Trace a request through the entire application
- Correlate logs from different modules/services
- Filter logs by specific request, user, or endpoint

### 6. **Client IP Detection**
Intelligently detects client IP with support for:
- Direct connections: Uses `request.getRemoteAddr()`
- Proxy scenarios: Reads `X-Forwarded-For` header
- Proxy chains: Extracts the first (original client) IP
- Fallback: Uses `X-Real-IP` header if available

## Log Format with MDC

With the Logback configuration from earlier, logs will appear as:
```
[2026-06-16 14:32:15.234] [http-nio-8082-exec-5] INFO  m.m.a.s.c.JwtAuthenticationFilter - Incoming request - Method: POST, Endpoint: POST /api/v1/customers, Client-IP: 192.168.1.100, Query: page=1&size=10, Request-ID: 7f8c3e4d-6b2a-11ec-8000-0242ac1a0001
```

**In the log file**, you can include MDC variables in the pattern. To enhance the logback-spring.xml:

```xml
<property name="LOG_PATTERN" value="[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level [%X{requestId}] [%X{username}] %logger{36} - %msg%n"/>
```

This would add the MDC values to every log line:
```
[2026-06-16 14:32:15.234] [http-nio-8082-exec-5] INFO  [7f8c3e4d-6b2a-11ec-8000-0242ac1a0001] [john.doe] m.m.a.s.c.JwtAuthenticationFilter - JWT token validated successfully
```

## Usage Example

### 1. **Trace a Specific Request Through Logs**

If a request fails with ID `7f8c3e4d-6b2a-11ec-8000-0242ac1a0001`, search logs for:
```bash
# In file
grep "7f8c3e4d-6b2a-11ec-8000-0242ac1a0001" logs/application-*.log

# Or with enhanced pattern showing requestId in output
grep "\[7f8c3e4d-6b2a-11ec-8000-0242ac1a0001\]" logs/application-*.log
```

This will show ALL logs from that request across all modules.

### 2. **Trace a Specific User's Requests**

```bash
grep "\[john.doe\]" logs/application-*.log
```

Shows all requests from that user.

### 3. **Trace Specific Endpoint**

```bash
grep "endpoint=POST /api/v1/customers" logs/application-*.log
```

Shows all requests to that endpoint.

## Code Changes Made

### Key Methods Added:

1. **`extractOrGenerateRequestId()`** - Gets or creates request ID
2. **`buildEndpointString()`** - Formats endpoint as "METHOD /path"
3. **`logRequestDetails()`** - Logs incoming request info
4. **`logResponseDetails()`** - Logs outgoing response info
5. **`getClientIpAddress()`** - Detects real client IP (with proxy support)
6. **`getStatusCategory()`** - Categorizes HTTP status codes

### MDC Cleanup

Critical: The filter properly cleans up MDC in a `finally` block to prevent:
- Memory leaks from orphaned context values
- Cross-request contamination in thread pools
- Context bleeding between requests handled by the same thread

## Integration with Logback

The enhanced filter integrates seamlessly with the Logback configuration:
- Works with hourly rolling logs
- Respects profile-specific logging levels
- Supports UTF-8 character encoding
- Compatible with development console and production file logging

## Example Workflow: Debugging a Failed Request

1. **User reports**: "My request to create a customer failed"
2. **You check logs** with today's date: `cat logs/application-2026-06-16_14.log`
3. **Find the request ID** from response headers or application
4. **Search logs**: `grep "12345689-abcd-ef01-2345" logs/application-*.log`
5. **See entire request lifecycle**:
   - When request arrived (IP, endpoint, query params)
   - JWT token validation status
   - User that made request
   - Operation performed
   - Response status
   - Any errors/warnings along the way

This makes debugging production issues dramatically faster and more accurate.

## Configuration Recommendations

To get the most from this enhanced logging:

### 1. **Update Logback Pattern** (in `logback-spring.xml`)
Replace the log pattern with MDC variables:
```xml
<property name="LOG_PATTERN" value="[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level [REQ:%X{requestId}] [USER:%X{username}] %logger{36} - %msg%n"/>
```

### 2. **Set Request ID Header in Client**
When making API calls, set custom request ID:
```bash
curl -H "X-Request-ID: my-custom-id-12345" https://api.example.com/endpoint
```

Or let the system generate one (recommended).

### 3. **Forward Request ID in Microservices**
If making downstream calls, extract and forward the request ID:
```java
request.getHeader("X-Request-ID")  // Extract from incoming request
// Forward to downstream service as X-Request-ID header
```

## Performance Considerations

- **Request ID generation**: Minimal overhead (UUID generation once per request)
- **MDC operations**: Very fast (HashMap put/remove)
- **Logging**: Async appender recommended for file I/O
- **IP detection**: Efficient (header parsing only)

No significant performance impact.

