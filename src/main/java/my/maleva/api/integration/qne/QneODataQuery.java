package my.maleva.api.integration.qne;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Query-string builder for QNE's OData-style dialect.
 *
 * <p>QNE takes plain parameter names — {@code filter}, {@code orderby},
 * {@code skip}, {@code top}, {@code select} — not the {@code $filter} form
 * standard OData uses. The grammar inside {@code filter} is documented in the
 * legacy {@code qneapilist} comments and reproduced by the factory helpers
 * here: {@code eq}, {@code ne}, {@code gt}, {@code lt}, {@code le},
 * {@code in [..]}, {@code startswith(..)}, {@code endswith(..)},
 * {@code contains(..)}.
 *
 * <p>Values are single-quoted the way the legacy call sites quoted them.
 * A single quote inside a value is doubled — the OData escape — because a
 * customer name like {@code O'BRIEN} otherwise truncates the expression.
 */
public final class QneODataQuery {

    private final Map<String, String> params = new LinkedHashMap<>();

    private QneODataQuery() {
    }

    public static QneODataQuery create() {
        return new QneODataQuery();
    }

    /** Quotes a literal for use inside a filter expression. */
    public static String quote(String value) {
        return "'" + (value == null ? "" : value.replace("'", "''")) + "'";
    }

    /** {@code field eq 'value'} */
    public static String eq(String field, String value) {
        return field + " eq " + quote(value);
    }

    /** {@code field ne 'value'} */
    public static String ne(String field, String value) {
        return field + " ne " + quote(value);
    }

    /** {@code contains(field,'value')} */
    public static String contains(String field, String value) {
        return "contains(" + field + "," + quote(value) + ")";
    }

    /** {@code startswith(field,'value')} */
    public static String startsWith(String field, String value) {
        return "startswith(" + field + "," + quote(value) + ")";
    }

    public QneODataQuery filter(String expression) {
        params.put("filter", expression);
        return this;
    }

    public QneODataQuery orderBy(String field) {
        params.put("orderby", field);
        return this;
    }

    public QneODataQuery skip(int skip) {
        params.put("skip", String.valueOf(skip));
        return this;
    }

    public QneODataQuery top(int top) {
        params.put("top", String.valueOf(top));
        return this;
    }

    public QneODataQuery select(String fields) {
        params.put("select", fields);
        return this;
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

    /** Immutable view for the URL builder to append. */
    public Map<String, String> params() {
        return Map.copyOf(params);
    }
}
