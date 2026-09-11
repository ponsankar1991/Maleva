package my.maleva.api.module.customerstatement.reply;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Who gets told about new customer replies — the roles in
 * {@code mail.statement.replies.notify-roles}, by id or by name
 * ("100,SUPERADMIN"). The JWT filter grants one authority per user,
 * {@code ROLE_<name>} for a known role and {@code ROLE_<id>} otherwise, so
 * both spellings are accepted and a role can be added in configuration
 * without touching code.
 */
@Component
public class NotifyRoles {

    private final Set<String> authorities;

    public NotifyRoles(@Value("${mail.statement.replies.notify-roles:100,SUPERADMIN}") String configured) {
        this.authorities = parse(configured);
    }

    static Set<String> parse(String configured) {
        if (configured == null || configured.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(configured.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> "ROLE_" + s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean allows(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority granted : auth.getAuthorities()) {
            if (granted.getAuthority() != null && authorities.contains(granted.getAuthority().toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public List<String> configured() {
        return authorities.stream().map(a -> a.substring("ROLE_".length())).sorted().toList();
    }
}
