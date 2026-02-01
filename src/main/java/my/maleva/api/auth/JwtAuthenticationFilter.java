package my.maleva.api.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import my.maleva.api.util.UserRoles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenStore tokenStore;

    public JwtAuthenticationFilter(JwtService jwtService, TokenStore tokenStore) {
        this.jwtService = jwtService;
        this.tokenStore = tokenStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                if (jwtService.validateToken(token) && tokenStore.exists(token)) {
                    String subject = jwtService.getSubject(token);

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
                }
            } catch (Exception e) {
                // validation failed - do not set authentication; the request will be unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
