package my.maleva.api.config;

import my.maleva.api.auth.JwtAuthenticationFilter;
import my.maleva.api.auth.JwtService;
import my.maleva.api.auth.TokenStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${security.password-encoding.enabled:true}")
    private boolean passwordEncodingEnabled;

    // Keep a PasswordEncoder bean available for services (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        if (passwordEncodingEnabled) {
            return new BCryptPasswordEncoder();
        }
        // NoOpPasswordEncoder is intentionally used only for development/testing when encoding is disabled
        @SuppressWarnings("deprecation")
        PasswordEncoder encoder = NoOpPasswordEncoder.getInstance();
        return encoder;
    }

    // UserDetailsService is provided by a DB-backed AppUserDetailsService (component) instead of in-memory here.
    // Example in-memory user for demos; replace with your UserDetailsService (DB) in production
    // @Bean
    // public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    //     UserDetails user = User.withUsername("user")
    //             .password(passwordEncoder.encode("password"))
    //             .roles("USER")
    //             .build();
    //     return new InMemoryUserDetailsManager(user);
    // }

    // Expose the AuthenticationManager from AuthenticationConfiguration
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        try {
            return authenticationConfiguration.getAuthenticationManager();
        } catch (Exception e) {
            // wrap in IllegalStateException to avoid checked exception on bean factory methods
            throw new IllegalStateException("Failed to get AuthenticationManager", e);
        }
    }

    // Security filter chain: register JWT filter and secure endpoints
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService, TokenStore tokenStore) {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, tokenStore);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        // public endpoints (call requestMatchers separately to avoid varargs resolution issues)
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // welcome requires JWT auth
                        .requestMatchers("/api/welcome").authenticated()
                        // all other endpoints require authentication
                        .anyRequest().authenticated()
                );

        try {
            return http.build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build SecurityFilterChain", e);
        }
    }
}
