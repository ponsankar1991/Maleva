package my.maleva.api.repo;

import lombok.RequiredArgsConstructor;
import my.maleva.api.dto.CustomerDto;
import my.maleva.api.dto.request.CustomerSelectRequest;
import my.maleva.api.dto.response.CustomerSelectDto;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustomerQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /* =========================
       COUNT QUERY
       ========================= */

    public long countCustomers(CustomerSelectRequest req) {

        StringBuilder sql = new StringBuilder("""
        SELECT COUNT(S.Id)
        FROM Customer S
        WHERE S.CompanyRefId = :companyId
          AND S.Active != 2
    """);

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", req.getCompanyId());

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            switch (req.getColumn()) {
                case "CustomerName" -> {
                    sql.append(" AND S.CustomerName LIKE :keyword");
                    params.put("keyword", "%" + req.getKeyword() + "%");
                }
                case "MobileNo" -> {
                    sql.append(" AND S.MobileNo LIKE :keyword");
                    params.put("keyword", "%" + req.getKeyword() + "%");
                }
                case "Id" -> {
                    sql.append(" AND S.Id = :id");
                    params.put("id", Integer.parseInt(req.getKeyword()));
                }
            }
        }

        Long count = jdbc.queryForObject(sql.toString(), params, Long.class);
        return count != null ? count : 0L;
    }


    /* =========================
       SELECT QUERY
       ========================= */
    public List<CustomerSelectDto> findCustomers(CustomerSelectRequest req) {

        StringBuilder sql = new StringBuilder("""
            SELECT
                S.Id,
                S.CompanyRefId,
                S.CustomerName,
                S.MobileNo,
                S.Email,
                S.Created_Date,
                A.SName AS SName,
                SM.TermsName AS TermsName,
                Ag.AccountCode AS AccountCode,
                CM.Country AS CMName
            FROM Customer S
            INNER JOIN SymbolMaster A ON S.SymbolRefid = A.Id
            INNER JOIN PaymentTermsMaster SM ON S.PaymentTermsRefid = SM.Id
            INNER JOIN AccountsGroupMaster Ag ON Ag.Id = S.AccountRefid
            LEFT JOIN CountryMaster CM ON S.countryId = CM.Id
            WHERE S.CompanyRefId = :companyId
              AND S.Active != 2
        """);

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", req.getCompanyId());

        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            switch (req.getColumn()) {
                case "CustomerName" -> {
                    sql.append(" AND S.CustomerName LIKE :keyword");
                    params.put("keyword", "%" + req.getKeyword() + "%");
                }
                case "MobileNo" -> {
                    sql.append(" AND S.MobileNo LIKE :keyword");
                    params.put("keyword", "%" + req.getKeyword() + "%");
                }
                case "Id" -> {
                    sql.append(" AND S.Id = :id");
                    params.put("id", Integer.parseInt(req.getKeyword()));
                }
            }
        }

        sql.append("""
            ORDER BY S.CustomerName
            OFFSET :startIndex ROWS
            FETCH NEXT :pageCount ROWS ONLY
        """);

        params.put("startIndex", Math.max(req.getStartIndex(), 0));
        params.put("pageCount", req.getPageCount());

        return jdbc.query(sql.toString(), params, (rs, rowNum) -> {
            var ts = rs.getTimestamp("Created_Date");
            return CustomerSelectDto.builder()
                    .id(rs.getInt("Id"))
                    .companyRefId(rs.getInt("CompanyRefId"))
                    .customerName(rs.getString("CustomerName"))
                    .mobileNo(rs.getString("MobileNo"))
                    .email(rs.getString("Email"))
                    .sName(rs.getString("SName"))
                    .termsName(rs.getString("TermsName"))
                    .accountCode(rs.getString("AccountCode"))
                    .country(rs.getString("CMName"))
                    .createdDate(ts != null ? ts.toLocalDateTime() : null)
                    .build();
        });
    }
}
