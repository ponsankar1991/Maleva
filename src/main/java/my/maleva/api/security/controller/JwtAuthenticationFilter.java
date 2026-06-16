package my.maleva.api.security.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import my.maleva.api.common.constant.UserRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC = "requestId";
    private static final String USER_MDC = "username";
    private static final String ENDPOINT_MDC = "endpoint";

    private final JwtService jwtService;
    private final TokenStore tokenStore;

    public JwtAuthenticationFilter(JwtService jwtService, TokenStore tokenStore) {
        this.jwtService = jwtService;
        this.tokenStore = tokenStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Generate or extract request ID for correlation
        String requestId = extractOrGenerateRequestId(request);

        // Set MDC values for logging correlation
        MDC.put(REQUEST_ID_MDC, requestId);
        MDC.put(ENDPOINT_MDC, buildEndpointString(request));

        try {
            logRequestDetails(request, requestId);

            String header = request.getHeader("Authorization");
            String authenticatedUser = null;

            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                String token = header.substring(7).trim();
                try {
                    if (jwtService.validateToken(token) && tokenStore.exists(token)) {
                        String subject = jwtService.getSubject(token);
                        authenticatedUser = subject;

                        // Extract roleId and map to authorities
                        Integer roleId = jwtService.getRoleId(token);
                        List<GrantedAuthority> authorities = Collections.emptyList();
                        if (roleId != null) {
                            Optional<UserRoles> maybeRole = UserRoles.fromId(roleId);
                            if (maybeRole.isPresent()) {
                                String roleName = maybeRole.get().name();
                                authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
                            } else {
                                // fallback: expose numeric role as authority
                                authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleId));
                            }
                        }

                        Authentication auth = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        log.info("JWT token validated successfully for user: {}, roles: {}", subject, authorities);
                    } else {
                        log.warn("JWT token validation failed or token not found in store");
                    }
                } catch (Exception e) {
                    // validation failed - do not set authentication; the request will be unauthenticated
                    log.warn("Exception during JWT validation: {}", e.getMessage());
                }
            } else {
                log.debug("No Bearer token found in Authorization header");
            }

            // Set username in MDC if authenticated
            if (authenticatedUser != null) {
                MDC.put(USER_MDC, authenticatedUser);
            }

            // Wrap response to capture body
            ResponseWrapper wrappedResponse = new ResponseWrapper(response);

            filterChain.doFilter(request, wrappedResponse);

            // Write captured response body back to original response
            String responseBody = wrappedResponse.getCapturedOutput();
            response.getWriter().write(responseBody);
            response.flushBuffer();

            // Log response details with body
            logResponseDetails(request, wrappedResponse, requestId, responseBody);

        } finally {
            // Clean up MDC to prevent memory leaks and thread pool contamination
            MDC.remove(REQUEST_ID_MDC);
            MDC.remove(USER_MDC);
            MDC.remove(ENDPOINT_MDC);
        }
    }

    /**
     * Extract request ID from header or generate a new one
     */
    private String extractOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    /**
     * Build endpoint string (method + URI)
     */
    private String buildEndpointString(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI();
    }

    /**
     * Log incoming request details
     */
    private void logRequestDetails(HttpServletRequest request, String requestId) {
        String endpoint = buildEndpointString(request);
        String clientIp = getClientIpAddress(request);
        String queryString = request.getQueryString();

        log.info("Incoming request - Method: {}, Endpoint: {}, Client-IP: {}, Query: {}, Request-ID: {}",
                request.getMethod(),
                endpoint,
                clientIp,
                queryString != null ? queryString : "none",
                requestId);
    }

    /**
     * Log response details including response body
     */
    private void logResponseDetails(HttpServletRequest request, HttpServletResponse response, String requestId, String responseBody) {
        String endpoint = buildEndpointString(request);
        int statusCode = response.getStatus();
        String statusCategory = getStatusCategory(statusCode);

        // Limit response body to 2000 characters to prevent excessively large logs
        String truncatedBody = responseBody;
        if (responseBody.length() > 2000) {
            truncatedBody = responseBody.substring(0, 2000) + "... [TRUNCATED - " + (responseBody.length() - 2000) + " more characters]";
        }

        log.info("Response sent - Endpoint: {}, Status: {} ({}), Body: {}, Request-ID: {}",
                endpoint,
                statusCode,
                statusCategory,
                truncatedBody,
                requestId);
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check for IP from proxy headers
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs (client, proxy1, proxy2, etc)
            // Get the first one which is the client's IP
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }

    /**
     * Categorize HTTP status codes
     */
    private String getStatusCategory(int statusCode) {
        if (statusCode < 300) {
            return "Success";
        } else if (statusCode < 400) {
            return "Redirect";
        } else if (statusCode < 500) {
            return "Client Error";
        } else {
            return "Server Error";
        }
    }

    /**
     * Custom HttpServletResponseWrapper to capture response body
     * Allows us to read response body while still sending it to the client
     */
    private static class ResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                output.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener listener) {
                // Not needed for this implementation
            }
        };

        private PrintWriter printWriter;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
            this.printWriter = new PrintWriter(output);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return servletOutputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return printWriter;
        }

        /**
         * Get the captured response output as a string
         */
        public String getCapturedOutput() {
            try {
                printWriter.flush();
                return output.toString(StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("Error getting captured response output: {}", e.getMessage());
                return "[Unable to capture response body]";
            }
        }
    }
}
