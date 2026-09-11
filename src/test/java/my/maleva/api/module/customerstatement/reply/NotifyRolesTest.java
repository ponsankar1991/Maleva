package my.maleva.api.module.customerstatement.reply;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Who sees the reply bell: the configured roles, by id or by name, as the JWT filter grants them. */
class NotifyRolesTest {

    private static Authentication with(String... authorities) {
        return new UsernamePasswordAuthenticationToken("mages", null,
                java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
    }

    @Test
    @DisplayName("the default: SUPERADMIN by name (a known role) or 100 by id (the fallback authority)")
    void defaults() {
        NotifyRoles roles = new NotifyRoles("100,SUPERADMIN");

        assertThat(roles.allows(with("ROLE_SUPERADMIN"))).isTrue();
        assertThat(roles.allows(with("ROLE_100"))).isTrue();
        assertThat(roles.allows(with("ROLE_ADMIN"))).isFalse();
        assertThat(roles.allows(with("ROLE_200"))).isFalse();
        assertThat(roles.allows(null)).isFalse();
        assertThat(roles.configured()).containsExactly("100", "SUPERADMIN");
    }

    @Test
    @DisplayName("a role added in configuration is honoured without a code change; spacing and case do not matter")
    void addedRole() {
        NotifyRoles roles = new NotifyRoles(" 100 ; superadmin, Accounts ,1100");

        assertThat(roles.allows(with("ROLE_ACCOUNTS"))).isTrue();
        assertThat(roles.allows(with("ROLE_1100"))).isTrue();
        assertThat(roles.allows(with("ROLE_SUPERADMIN"))).isTrue();
        assertThat(roles.allows(with("ROLE_300"))).isFalse();
    }

    @Test
    @DisplayName("an empty list means nobody")
    void nobody() {
        assertThat(new NotifyRoles("").allows(with("ROLE_SUPERADMIN"))).isFalse();
        assertThat(NotifyRoles.parse(null)).isEmpty();
    }
}
