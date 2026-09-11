package my.maleva.api.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** The one WebSocket endpoint in the application. */
    static final String WS_ENDPOINT = "/api/ws";

    private static final String EXTENSIONS_HEADER = "Sec-WebSocket-Extensions";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the messages back to the client on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic");
        
        // Prefix for messages that are bound for methods annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint that clients will use to connect to the WebSocket server
        // Using setAllowedOriginPatterns("*") to be flexible, but in production this should be restricted to specific domains (e.g. your React app)
        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns("*"); // Raw WebSockets for @stomp/stompjs
    }

    /**
     * Hides the client's <code>Sec-WebSocket-Extensions</code> header from the
     * handshake, which switches off <code>permessage-deflate</code> — WebSocket
     * frame compression.
     *
     * Every browser (and Node) offers permessage-deflate; curl and .NET's
     * ClientWebSocket do not. This endpoint used to accept it, and the resulting
     * compressed frames do not survive the IIS/ARR reverse proxy in front of
     * Tomcat: the handshake returns 101, an idle socket stays open, and the
     * connection dies with code 1006 the moment the client sends its first
     * frame. The STOMP CONNECTED frame therefore never arrives and Planning's
     * live-update dot stays red — for browsers only, while command-line probes
     * reported everything healthy.
     *
     * It has to be done here, at the servlet layer. The obvious route —
     * overriding {@code filterRequestedExtensions} on a {@code HandshakeHandler}
     * — looks right and does nothing: Tomcat's {@code UpgradeUtil.doUpgrade}
     * appends its own {@code Constants.INSTALLED_EXTENSIONS} (which contains
     * permessage-deflate) to whatever Spring passes down, and there is no
     * property to disable that. With the header gone there is nothing left for
     * Tomcat to negotiate.
     *
     * Nothing is lost by dropping compression: this channel carries short STOMP
     * control frames and a one-word "UPDATE" broadcast.
     */
    @Bean
    public FilterRegistrationBean<Filter> webSocketCompressionDisablingFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(new StripExtensionsFilter());
        registration.addUrlPatterns(WS_ENDPOINT);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("webSocketCompressionDisablingFilter");
        return registration;
    }

    static class StripExtensionsFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            if (request instanceof HttpServletRequest http
                    && "websocket".equalsIgnoreCase(String.valueOf(http.getHeader("Upgrade")).trim())) {
                chain.doFilter(new ExtensionsHiddenRequest(http), response);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    /** The request as the handshake should see it: without any extension offer. */
    static class ExtensionsHiddenRequest extends HttpServletRequestWrapper {

        ExtensionsHiddenRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            return EXTENSIONS_HEADER.equalsIgnoreCase(name) ? null : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return EXTENSIONS_HEADER.equalsIgnoreCase(name)
                    ? Collections.emptyEnumeration()
                    : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> kept = new ArrayList<>();
            for (Enumeration<String> names = super.getHeaderNames(); names.hasMoreElements(); ) {
                String name = names.nextElement();
                if (!EXTENSIONS_HEADER.equalsIgnoreCase(name)) {
                    kept.add(name);
                }
            }
            return Collections.enumeration(kept);
        }
    }
}
