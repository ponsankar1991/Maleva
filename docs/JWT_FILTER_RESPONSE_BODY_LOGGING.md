# JWT Filter Enhancement v2 - Response Body Logging

## New Feature: Response Body Logging

The JWT authentication filter has been further enhanced to also **capture and log the response body**, providing complete end-to-end visibility of API requests and responses.

## What's New

### Response Body Capture
The filter now:
- ✅ Captures the complete response body written by your application
- ✅ Logs the response body alongside status code and endpoint
- ✅ Automatically truncates large responses (2000 character limit) to prevent excessive logging
- ✅ Sends the body to the client normally (no disruption)

### How It Works

1. **Response Wrapping**: Uses a custom `ResponseWrapper` that intercepts writes to the response output stream
2. **Buffering**: Captures data in a `ByteArrayOutputStream` while maintaining compatibility with the client
3. **Logging**: After the response is generated, logs the captured body to the application logs
4. **Cleanup**: Properly flushes buffers and sends data to client

## Sample Log Output

### Before (Without Response Body)
```
[2026-06-16 14:32:15.456] INFO  [REQ:550e8400-e29b] [USER:john.doe] - Response sent - Endpoint: POST /api/v1/customers, Status: 201 (Success), Request-ID: 550e8400-e29b
```

### After (With Response Body)
```
[2026-06-16 14:32:15.456] INFO  [REQ:550e8400-e29b] [USER:john.doe] - Response sent - Endpoint: POST /api/v1/customers, Status: 201 (Success), Body: {"id":12345,"name":"Acme Corp","email":"info@acme.com","status":"ACTIVE","createdAt":"2026-06-16T14:32:15"}, Request-ID: 550e8400-e29b
```

## Real-World Examples

### Example 1: Successful Customer Creation

**Request:**
```bash
curl -X POST \
  -H "Authorization: Bearer valid_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme Corp","email":"info@acme.com"}' \
  http://localhost:8082/api/v1/customers
```

**Log Output:**
```
[2026-06-16 14:35:22.123] INFO  [REQ:create-001] [USER:john.doe] - Incoming request - Method: POST, Endpoint: POST /api/v1/customers, Client-IP: 192.168.1.100, Query: none, Request-ID: create-001

[2026-06-16 14:35:22.145] INFO  [REQ:create-001] [USER:john.doe] - JWT token validated successfully for user: john.doe, roles: [ROLE_ADMIN]

[2026-06-16 14:35:22.456] INFO  [REQ:create-001] [USER:john.doe] - Response sent - Endpoint: POST /api/v1/customers, Status: 201 (Success), Body: {"id":12345,"name":"Acme Corp","email":"info@acme.com","status":"ACTIVE","createdAt":"2026-06-16T14:35:22"}, Request-ID: create-001
```

**What You Can See:**
- Request came with JWT token under user `john.doe`
- Authentication succeeded with ROLE_ADMIN
- API created customer with ID 12345
- Response status was 201 (Created)
- All correlated with request ID: `create-001`

### Example 2: Error Response With Details

**Request:**
```bash
curl -X POST \
  -H "Authorization: Bearer valid_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{"name":""}' \
  http://localhost:8082/api/v1/customers  # Missing required fields
```

**Log Output:**
```
[2026-06-16 14:40:30.123] INFO  [REQ:error-001] [USER:john.doe] - Incoming request - Method: POST, Endpoint: POST /api/v1/customers, Client-IP: 192.168.1.100, Query: none, Request-ID: error-001

[2026-06-16 14:40:30.145] INFO  [REQ:error-001] [USER:john.doe] - JWT token validated successfully for user: john.doe, roles: [ROLE_ADMIN]

[2026-06-16 14:40:30.250] ERROR [REQ:error-001] [USER:john.doe] - Validation failed: name is required

[2026-06-16 14:40:30.350] INFO  [REQ:error-001] [USER:john.doe] - Response sent - Endpoint: POST /api/v1/customers, Status: 400 (Client Error), Body: {"error":"Validation failed","message":"name is required","timestamp":"2026-06-16T14:40:30","path":"/api/v1/customers"}, Request-ID: error-001
```

**What You Can See:**
- Request validation failed with specific error message
- Response status is 400 (Bad Request)
- Full error details in response body for debugging

### Example 3: Large Response (Truncated)

**Sample Log for list endpoint:**
```
[2026-06-16 14:45:15.500] INFO  [REQ:list-001] [USER:jane.smith] - Response sent - Endpoint: GET /api/v1/customers, Status: 200 (Success), Body: {"items":[{"id":1,"name":"Acme Corp",...},{...},...], "total":500, "page":1, "size":10}... [TRUNCATED - 15234 more characters], Request-ID: list-001
```

**What You See:**
- Response is truncated at 2000 characters
- Shows how many characters were truncated
- Prevents excessively large log entries

## Performance Considerations

### Memory Usage
- ✅ **Minimal**: Response bodies buffered in memory only temporarily (during request processing)
- ✅ **Automatic cleanup**: ByteArrayOutputStream is garbage collected after request completes
- ✅ **Per-request**: New buffer created for each request (thread-safe)

### Throughput
- ✅ **Negligible impact**: Buffering happens in parallel with normal response processing
- ✅ **No blocking**: Filter doesn't wait for logging - async where possible
- ✅ **Optimized**: Uses efficient ByteArrayOutputStream implementation

### When to Disable

If you notice performance issues with large responses:

1. **Development environment**: Keep enabled for debugging
2. **Production with high volume**: Disable logging large responses:

```java
// Modify logResponseDetails to conditionally log based on response size
if (responseBody.length() < 5000) {
    log.info("Response sent - Endpoint: {}, Status: {} ({}), Body: {}, Request-ID: {}",
            endpoint, statusCode, statusCategory, truncatedBody, requestId);
} else {
    log.info("Response sent - Endpoint: {}, Status: {} ({}), Body size: {} bytes, Request-ID: {}",
            endpoint, statusCode, statusCategory, responseBody.length(), requestId);
}
```

## Debugging Workflows

### Trace Failed Order Creation

**User reports**: "Order creation failed, got 400 error"

**Step 1: Search logs for the request:**
```bash
grep "Endpoint: POST /api/v1/orders.*Status: 400" logs/application-*.log
# Output shows request ID: order-fail-789
```

**Step 2: Get request ID and trace it:**
```bash
grep "order-fail-789" logs/application-*.log
# Shows incoming request, auth, validation error, response with full error details
```

**Step 3: Read response body in log:**
```
Body: {"error":"OrderValidationError","message":"Customer not found: 99999","field":"customerId","timestamp":"2026-06-16T14:40:30"}
```

**Result**: Immediately understand the root cause (customer ID 99999 doesn't exist)

### Monitor API Success/Failure Rates

```bash
# Count successful responses (status 200-299)
grep "Status: [12][0-9][0-9]" logs/application-*.log | wc -l

# Count error responses
grep "Status: [45][0-9][0-9]" logs/application-*.log | wc -l

# See error details
grep "Status: [45][0-9][0-9]" logs/application-*.log | grep "Body:"
```

## Configuration Options

### Adjust Truncation Limit

In `logResponseDetails()` method, change the truncation threshold:

```java
// Current: 2000 characters
if (responseBody.length() > 2000) {

// Change to: 5000 characters for more detail
if (responseBody.length() > 5000) {

// Or: 500 characters for less verbosity
if (responseBody.length() > 500) {
```

### Log Response Body Only for Errors

```java
// Only log body for error responses
if (statusCode >= 400 && responseBody.length() < 2000) {
    log.info("Response ERROR body: {}", responseBody);
}
```

### Log Response Body to Separate Logger

```java
// Create a separate logger for response bodies
private static final Logger responseLog = LoggerFactory.getLogger("RESPONSE_LOGGER");

// Then in logResponseDetails:
if (statusCode >= 400) {
    responseLog.info("Error response: {}", truncatedBody);
}
```

## Benefits

### For Developers
- ✅ See exactly what your API is returning
- ✅ Debug test failures quickly
- ✅ Understand API contract implementation

### For Operators
- ✅ Investigate customer complaints with full context
- ✅ Monitor response formats in production
- ✅ Detect unexpected response structures

### For Debugging
- ✅ Complete request-response correlation via request ID
- ✅ See validation errors immediately
- ✅ Understand error conditions without external tools

## Technical Implementation

### ResponseWrapper Details

The custom `ResponseWrapper` class:
- Extends `HttpServletResponseWrapper` (standard servlet API)
- Captures writes via custom `ServletOutputStream`
- Buffers output in `ByteArrayOutputStream`
- Provides `getCapturedOutput()` to read captured data
- Handles exceptions gracefully

### Thread Safety
- ✅ Each request gets its own ResponseWrapper instance
- ✅ ByteArrayOutputStream is thread-local to the request
- ✅ No shared state between concurrent requests
- ✅ Safe for high-concurrency environments

## Complete Log Example: End-to-End Request

```
# 1. REQUEST ARRIVES
[2026-06-16 15:00:00.100] INFO  [REQ:workflow-123] [USER:N/A] - Incoming request - Method: POST, Endpoint: POST /api/v1/orders, Client-IP: 192.168.1.50, Query: none, Request-ID: workflow-123

# 2. JWT VALIDATED
[2026-06-16 15:00:00.150] INFO  [REQ:workflow-123] [USER:alice.wonder] - JWT token validated successfully for user: alice.wonder, roles: [ROLE_USER]

# 3. BUSINESS LOGIC EXECUTES
[2026-06-16 15:00:00.200] INFO  [REQ:workflow-123] [USER:alice.wonder] - Processing order for customer: 5001
[2026-06-16 15:00:00.250] DEBUG [REQ:workflow-123] [USER:alice.wonder] - Checking inventory for product: P123
[2026-06-16 15:00:00.300] DEBUG [REQ:workflow-123] [USER:alice.wonder] - Stock available: 50 units
[2026-06-16 15:00:00.350] INFO  [REQ:workflow-123] [USER:alice.wonder] - Order created: ORD-2026-0001

# 4. RESPONSE SENT
[2026-06-16 15:00:00.400] INFO  [REQ:workflow-123] [USER:alice.wonder] - Response sent - Endpoint: POST /api/v1/orders, Status: 201 (Success), Body: {"id":"ORD-2026-0001","customerId":5001,"items":[{"productId":"P123","quantity":10,"price":99.99}],"total":999.90,"status":"CREATED","timestamp":"2026-06-16T15:00:00"}, Request-ID: workflow-123

# Note: All logs include [REQ:workflow-123] for easy correlation
```

## Summary

Response body logging adds the final piece to complete observability:

- ✅ See request coming in (request logging)
- ✅ See auth decision (JWT logging)
- ✅ See business logic details (application logs)
- ✅ **NOW**: See exactly what response was sent (response body logging)
- ✅ Correlate everything with request ID

This is enterprise-grade request/response logging! 🎉

