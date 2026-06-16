# JWT Filter Enhanced Logging - Real-World Examples

## Sample Log Scenarios

### Scenario 1: Successful Authentication and Request Processing

**API Call:**
```bash
curl -X POST \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "X-Request-ID: req-2026-06-16-001" \
  -H "Content-Type: application/json" \
  -d '{"name": "Acme Corp", "email": "info@acme.com"}' \
  http://localhost:8082/api/v1/customers
```

**Log Output:**
```
[2026-06-16 14:32:15.234] [http-nio-8082-exec-5] INFO  [REQ:req-2026-06-16-001] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - Incoming request - Method: POST, Endpoint: POST /api/v1/customers, Client-IP: 192.168.1.100, Query: none, Request-ID: req-2026-06-16-001

[2026-06-16 14:32:15.245] [http-nio-8082-exec-5] INFO  [REQ:req-2026-06-16-001] [USER:john.doe] m.m.a.s.c.JwtAuthenticationFilter - JWT token validated successfully for user: john.doe, roles: [ROLE_ADMIN]

[2026-06-16 14:32:15.300] [http-nio-8082-exec-5] INFO  [REQ:req-2026-06-16-001] [USER:john.doe] m.m.a.m.customer.service.impl.CustomerServiceImpl - Creating customer: Acme Corp

[2026-06-16 14:32:15.350] [http-nio-8082-exec-5] INFO  [REQ:req-2026-06-16-001] [USER:john.doe] m.m.a.m.customer.controller.CustomerController - Customer created successfully with ID: 12345

[2026-06-16 14:32:15.456] [http-nio-8082-exec-5] INFO  [REQ:req-2026-06-16-001] [USER:john.doe] m.m.a.s.c.JwtAuthenticationFilter - Response sent - Endpoint: POST /api/v1/customers, Status: 201 (Success), Request-ID: req-2026-06-16-001
```

### Scenario 2: Invalid/Expired JWT Token

**API Call:**
```bash
curl -X GET \
  -H "Authorization: Bearer expired_or_invalid_token" \
  http://localhost:8082/api/v1/customers
```

**Log Output:**
```
[2026-06-16 14:35:22.123] [http-nio-8082-exec-3] INFO  [REQ:550e8400-e29b-41d4-a716-446655440000] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - Incoming request - Method: GET, Endpoint: GET /api/v1/customers, Client-IP: 192.168.1.101, Query: none, Request-ID: 550e8400-e29b-41d4-a716-446655440000

[2026-06-16 14:35:22.145] [http-nio-8082-exec-3] WARN  [REQ:550e8400-e29b-41d4-a716-446655440000] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - JWT token validation failed or token not found in store

[2026-06-16 14:35:22.200] [http-nio-8082-exec-3] WARN  [REQ:550e8400-e29b-41d4-a716-446655440000] [USER:N/A] m.m.a.s.c.SecurityExceptionHandler - Unauthorized access attempt to endpoint: GET /api/v1/customers

[2026-06-16 14:35:22.350] [http-nio-8082-exec-3] INFO  [REQ:550e8400-e29b-41d4-a716-446655440000] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - Response sent - Endpoint: GET /api/v1/customers, Status: 401 (Client Error), Request-ID: 550e8400-e29b-41d4-a716-446655440000
```

### Scenario 3: Missing Authorization Header

**API Call:**
```bash
curl -X GET http://localhost:8082/api/v1/customers
```

**Log Output:**
```
[2026-06-16 14:40:10.567] [http-nio-8082-exec-1] INFO  [REQ:750f9c01-a30d-42e5-b82a-557765550111] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - Incoming request - Method: GET, Endpoint: GET /api/v1/customers, Client-IP: 192.168.1.102, Query: none, Request-ID: 750f9c01-a30d-42e5-b82a-557765550111

[2026-06-16 14:40:10.578] [http-nio-8082-exec-1] DEBUG [REQ:750f9c01-a30d-42e5-b82a-557765550111] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - No Bearer token found in Authorization header

[2026-06-16 14:40:10.650] [http-nio-8082-exec-1] INFO  [REQ:750f9c01-a30d-42e5-b82a-557765550111] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - Response sent - Endpoint: GET /api/v1/customers, Status: 401 (Client Error), Request-ID: 750f9c01-a30d-42e5-b82a-557765550111
```

### Scenario 4: Request Through Proxy/Load Balancer

**API Call from behind proxy:**
```bash
# Client makes request through proxy, serves sees X-Forwarded-For
```

**Log Output:**
```
[2026-06-16 15:10:45.234] [http-nio-8082-exec-7] INFO  [REQ:abc123def456] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - Incoming request - Method: POST, Endpoint: POST /api/v1/orders, Client-IP: 203.0.113.15, Query: none, Request-ID: abc123def456
                                                                                                                                              ↑
                                                                                              Real client IP extracted from X-Forwarded-For header
```

### Scenario 5: Server Error During Processing

**API Call:**
```bash
curl -X DELETE \
  -H "Authorization: Bearer valid_token" \
  http://localhost:8082/api/v1/customers/99999
```

**Log Output:**
```
[2026-06-16 16:20:30.123] [http-nio-8082-exec-2] INFO  [REQ:err-999-delete] [USER:N/A] m.m.a.s.c.JwtAuthenticationFilter - Incoming request - Method: DELETE, Endpoint: DELETE /api/v1/customers/99999, Client-IP: 192.168.1.200, Query: none, Request-ID: err-999-delete

[2026-06-16 16:20:30.145] [http-nio-8082-exec-2] INFO  [REQ:err-999-delete] [USER:jane.smith] m.m.a.s.c.JwtAuthenticationFilter - JWT token validated successfully for user: jane.smith, roles: [ROLE_ADMIN]

[2026-06-16 16:20:30.200] [http-nio-8082-exec-2] INFO  [REQ:err-999-delete] [USER:jane.smith] m.m.a.m.customer.service.impl.CustomerServiceImpl - Deleting customer with ID: 99999

[2026-06-16 16:20:30.250] [http-nio-8082-exec-2] ERROR [REQ:err-999-delete] [USER:jane.smith] m.m.a.m.customer.service.impl.CustomerServiceImpl - Customer not found: 99999, java.lang.EntityNotFoundException: Customer 99999 not found
        at my.maleva.api.module.customer.service.impl.CustomerServiceImpl.findById(CustomerServiceImpl.java:45)
        ...

[2026-06-16 16:20:30.350] [http-nio-8082-exec-2] INFO  [REQ:err-999-delete] [USER:jane.smith] m.m.a.s.c.JwtAuthenticationFilter - Response sent - Endpoint: DELETE /api/v1/customers/99999, Status: 404 (Client Error), Request-ID: err-999-delete
```

## Debugging with Request IDs

### Example: Debugging a Failed Order Creation

**User reports**: "My order creation failed with status 500"

**Step 1: Check application logs from today**
```bash
# Find the failed request
grep "Status: 500" logs/application-2026-06-16_*.log

# Output shows:
# [2026-06-16 16:45:30.456] [http-nio-8082-exec-6] INFO [REQ:order-fail-789] [USER:bob.johnson] ... Status: 500 (Server Error)
```

**Step 2: Extract the request ID: `order-fail-789`**

**Step 3: Trace entire request lifecycle**
```bash
grep "order-fail-789" logs/application-2026-06-16_*.log
```

**Output shows complete sequence:**
```
[Request incoming]
[JWT validation passed → user: bob.johnson]
[Business logic started]
[Database operation started]
[ERROR: Connection timeout]
[Rollback initiated]
[Response sent: 500]
```

Now you know exactly where and why the request failed!

## Log File Examples (Production)

### logs/application-2026-06-16_14.log
```
[2026-06-16 14:00:01.234] [http-nio-8082-exec-1] INFO  [REQ:morning-req-001] [USER:admin] m.m.a.s.c.JwtAuthenticationFilter - Incoming request - Method: GET, Endpoint: GET /api/v1/dashboard, Client-IP: 192.168.1.50, Query: none, Request-ID: morning-req-001
[2026-06-16 14:00:01.250] [http-nio-8082-exec-1] INFO  [REQ:morning-req-001] [USER:admin] m.m.a.s.c.JwtAuthenticationFilter - JWT token validated successfully for user: admin, roles: [ROLE_ADMIN]
[2026-06-16 14:00:02.100] [http-nio-8082-exec-1] INFO  [REQ:morning-req-001] [USER:admin] m.m.a.s.c.JwtAuthenticationFilter - Response sent - Endpoint: GET /api/v1/dashboard, Status: 200 (Success), Request-ID: morning-req-001

[2026-06-16 14:05:15.567] [http-nio-8082-exec-2] INFO  [REQ:morning-req-002] [USER:user1] m.m.a.s.c.JwtAuthenticationFilter - Incoming request - Method: POST, Endpoint: POST /api/v1/sales-orders, Client-IP: 192.168.1.51, Query: none, Request-ID: morning-req-002
[2026-06-16 14:05:15.580] [http-nio-8082-exec-2] INFO  [REQ:morning-req-002] [USER:user1] m.m.a.s.c.JwtAuthenticationFilter - JWT token validated successfully for user: user1, roles: [ROLE_USER]
[2026-06-16 14:05:16.234] [http-nio-8082-exec-2] INFO  [REQ:morning-req-002] [USER:user1] m.m.a.s.c.JwtAuthenticationFilter - Response sent - Endpoint: POST /api/v1/sales-orders, Status: 201 (Success), Request-ID: morning-req-002

...
```

## Advanced Log Filtering Commands

### Find all requests from a specific user:
```bash
grep "\[USER:john.doe\]" logs/application-*.log
```

### Find all failed requests (status >= 400):
```bash
grep "Status: [4-5][0-9][0-9]" logs/application-*.log
```

### Find slowest requests (process more than 5 seconds):
```bash
# Requires custom parsing, but you have the timestamps in logs
grep -E "Incoming request|Response sent" logs/application-*.log
```

### Count requests by status category:
```bash
grep "Success\|Redirect\|Client Error\|Server Error" logs/application-*.log | sort | uniq -c
```

### Find specific endpoint activity:
```bash
grep "Endpoint: POST /api/v1/customers" logs/application-*.log
```

## Summary

The enhanced JWT filter provides complete request/response visibility with:
- ✅ Unique request ID for correlation
- ✅ Authenticated user tracking
- ✅ Endpoint information
- ✅ Status codes and categories
- ✅ Client IP addresses (proxy-aware)
- ✅ Full request lifecycle logging
- ✅ Perfect for debugging production issues

