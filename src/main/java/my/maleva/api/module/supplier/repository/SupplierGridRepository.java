package my.maleva.api.module.supplier.repository;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.supplier.dto.SupplierGridPage;
import my.maleva.api.module.supplier.dto.SupplierGridRequest;
import my.maleva.api.module.supplier.dto.SupplierListRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * The SupplierView grid's search, on the server — the port of legacy
 * {@code SupplierServices.SelectSupplier} and of the grid's filter row.
 *
 * <p>Legacy loaded 3,000 rows into the browser and filtered only those, so a
 * supplier outside the loaded page could not be found; its "All" column
 * filtered nothing. Here every search runs against the whole company:
 * keyword, per-column filters, type, active flag, sort and paging in one SQL
 * statement, with the total from {@code COUNT(*) OVER ()} in the same trip.
 *
 * <p><b>Safe by construction.</b> Column and sort names come only from the
 * whitelists below — request text never reaches the SQL string. Every value
 * is a bound parameter, and LIKE wildcards typed by the user ({@code %},
 * {@code _}, {@code [}) are escaped, so "100%" finds "100%" rather than
 * everything starting with 100.
 *
 * <p>The joins read {@code WITH (NOLOCK)} as legacy SelectSupplier did, so a
 * long save never freezes the grid. A contains-search cannot use an index on
 * any column, so it scans the company's suppliers; that is a few dozen pages
 * at today's sizes, and the result is paged before it leaves the server.
 */
@Repository
@RequiredArgsConstructor
public class SupplierGridRepository {

    static final int DEFAULT_PAGE_SIZE = 50;
    static final int MAX_PAGE_SIZE = 500;

    private static final String FROM =
            " FROM Supplier s WITH (NOLOCK)"
            + " LEFT JOIN SymbolMaster sm WITH (NOLOCK) ON sm.Id = s.SymbolRefid"
            + " LEFT JOIN PaymentTermsMaster pt WITH (NOLOCK) ON pt.Id = s.PaymentTermsRefid"
            + " LEFT JOIN AccountsGroupMaster ag WITH (NOLOCK) ON ag.Id = s.AccountRefid";

    private static final String SELECT =
            "SELECT s.Id, ag.AccountCode, s.QNECode, s.SupplierName, s.SupplierType, sm.SName, pt.TermsName,"
            + " s.Address1, s.Address2, s.City, s.MobileNo, s.GSTNO, s.Active, COUNT(*) OVER () AS TotalRows";

    private record Column(String sql, Function<SupplierGridRequest, String> filter) {
    }

    /** The grid's columns: request key → SQL column, and where the column's filter text comes from. */
    static final Map<String, Column> COLUMNS = new LinkedHashMap<>();

    static {
        COLUMNS.put("accountCode", new Column("ag.AccountCode", SupplierGridRequest::getAccountCode));
        COLUMNS.put("qneCode", new Column("s.QNECode", SupplierGridRequest::getQneCode));
        COLUMNS.put("supplierName", new Column("s.SupplierName", SupplierGridRequest::getSupplierName));
        COLUMNS.put("supplierType", new Column("s.SupplierType", SupplierGridRequest::getSupplierType));
        COLUMNS.put("symbolName", new Column("sm.SName", SupplierGridRequest::getSymbolName));
        COLUMNS.put("termsName", new Column("pt.TermsName", SupplierGridRequest::getTermsName));
        COLUMNS.put("address1", new Column("s.Address1", SupplierGridRequest::getAddress1));
        COLUMNS.put("address2", new Column("s.Address2", SupplierGridRequest::getAddress2));
        COLUMNS.put("city", new Column("s.City", SupplierGridRequest::getCity));
        COLUMNS.put("mobileNo", new Column("s.MobileNo", SupplierGridRequest::getMobileNo));
        COLUMNS.put("gstNo", new Column("s.GSTNO", SupplierGridRequest::getGstNo));
    }

    private final NamedParameterJdbcTemplate jdbc;

    public SupplierGridPage search(SupplierGridRequest request) {
        Query query = build(request);

        long[] total = {0};
        List<SupplierListRow> items = jdbc.query(query.selectSql(), query.params(), (rs, rowNum) -> {
            total[0] = rs.getLong("TotalRows");
            return new SupplierListRow(
                    rs.getInt("Id"),
                    rs.getString("AccountCode"),
                    rs.getString("QNECode"),
                    rs.getString("SupplierName"),
                    rs.getString("SupplierType"),
                    rs.getString("SName"),
                    rs.getString("TermsName"),
                    rs.getString("Address1"),
                    rs.getString("Address2"),
                    rs.getString("City"),
                    rs.getString("MobileNo"),
                    rs.getString("GSTNO"),
                    rs.getInt("Active"));
        });

        // A page past the end has no rows to carry the window total; count
        // separately so the screen can still say how many there are.
        if (items.isEmpty() && query.page() > 0) {
            Long count = jdbc.queryForObject(query.countSql(), query.params(), Long.class);
            total[0] = count == null ? 0 : count;
        }
        return new SupplierGridPage(items, total[0], query.page(), query.size());
    }

    /** The statement and its parameters, separated from execution so it can be tested without a database. */
    record Query(String selectSql, String countSql, MapSqlParameterSource params, int page, int size) {
    }

    static Query build(SupplierGridRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource("comid", request.getCompanyId());
        StringBuilder where = new StringBuilder(" WHERE s.CompanyRefId = :comid AND s.Active <> 2");

        String type = trimToNull(request.getType());
        if (type != null && !"ALL".equalsIgnoreCase(type)) {
            where.append(" AND s.SupplierType = :type");
            params.addValue("type", type, Types.VARCHAR);
        }

        String active = trimToNull(request.getActive());
        if ("active".equalsIgnoreCase(active)) {
            where.append(" AND s.Active = 1");
        } else if ("inactive".equalsIgnoreCase(active)) {
            where.append(" AND s.Active = 0");
        }

        String keyword = trimToNull(request.getKeyword());
        if (keyword != null) {
            params.addValue("keyword", containsPattern(keyword), Types.VARCHAR);
            StringJoiner anyColumn = new StringJoiner(" OR ", " AND (", ")");
            for (Column column : COLUMNS.values()) {
                anyColumn.add(column.sql() + " LIKE :keyword ESCAPE '\\'");
            }
            if (keyword.matches("\\d{1,9}")) {
                anyColumn.add("s.Id = :keywordId");
                params.addValue("keywordId", Integer.parseInt(keyword), Types.INTEGER);
            }
            where.append(anyColumn);
        }

        int index = 0;
        for (Column column : COLUMNS.values()) {
            String value = trimToNull(column.filter().apply(request));
            if (value != null) {
                String name = "filter" + index++;
                where.append(" AND ").append(column.sql()).append(" LIKE :").append(name).append(" ESCAPE '\\'");
                params.addValue(name, containsPattern(value), Types.VARCHAR);
            }
        }

        Column sortColumn = request.getSortBy() == null ? null : COLUMNS.get(request.getSortBy());
        String direction = "asc".equalsIgnoreCase(request.getSortDir()) ? "ASC" : "DESC";
        String orderBy = sortColumn == null
                ? " ORDER BY s.Id " + direction
                : " ORDER BY " + sortColumn.sql() + " " + direction + ", s.Id DESC";

        int size = request.getSize() == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(MAX_PAGE_SIZE, request.getSize()));
        int page = request.getPage() == null ? 0 : Math.max(0, request.getPage());
        params.addValue("offset", (long) page * size, Types.BIGINT);
        params.addValue("size", size, Types.INTEGER);

        String selectSql = SELECT + FROM + where + orderBy + " OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY";
        String countSql = "SELECT COUNT(*)" + FROM + where;
        return new Query(selectSql, countSql, params, page, size);
    }

    /** {@code %text%} with LIKE's own wildcards escaped by {@code \}. */
    static String containsPattern(String text) {
        String escaped = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_").replace("[", "\\[");
        return "%" + escaped + "%";
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
