package my.maleva.api.module.ai.common;

import my.maleva.api.module.accounting.entity.GLAccounts;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Resolves the account code (or name) a model suggests to a GLAccounts row. */
public final class GlAccountMatcher {

    private final Map<String, GLAccounts> byCode = new HashMap<>();
    private final Map<String, GLAccounts> byCompactCode = new HashMap<>();
    private final Map<String, GLAccounts> byName = new HashMap<>();

    public GlAccountMatcher(List<GLAccounts> accounts) {
        for (GLAccounts account : accounts) {
            if (account == null || account.getRowIndex() == null) {
                continue;
            }
            String code = norm(account.getGlAccountCode());
            if (!code.isEmpty()) {
                byCode.putIfAbsent(code, account);
                byCompactCode.putIfAbsent(compact(code), account);
            }
            String name = norm(account.getDescription());
            if (!name.isEmpty()) {
                byName.putIfAbsent(name, account);
            }
        }
    }

    public Optional<GLAccounts> match(String codeOrName) {
        String value = norm(codeOrName);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        // Models sometimes echo the whole "code | name" line.
        int bar = value.indexOf('|');
        String code = bar > 0 ? value.substring(0, bar).trim() : value;
        GLAccounts account = byCode.get(code);
        if (account == null) {
            account = byCompactCode.get(compact(code));
        }
        if (account == null) {
            account = byName.get(value);
        }
        if (account == null && bar > 0) {
            account = byName.get(value.substring(bar + 1).trim());
        }
        return Optional.ofNullable(account);
    }

    private static String norm(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static String compact(String code) {
        return code.replaceAll("[^A-Z0-9]", "");
    }
}
