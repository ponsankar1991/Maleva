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
                                           S.*,
                                           A.SName AS SName,
                                           SM.TermsName AS TermsName,
                                           Ag.AccountCode AS AccountCode,
                                           CM.Country AS CMName,
                                           S.countryId AS countryId
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
            ORDER BY S.Id
            OFFSET :startIndex ROWS
            FETCH NEXT :pageCount ROWS ONLY
        """);

        params.put("startIndex", Math.max(req.getStartIndex(), 0));
        params.put("pageCount", req.getPageCount());

        return jdbc.query(sql.toString(), params, (rs, rowNum) -> {
            var createdTs = rs.getTimestamp("Created_Date");
            var modifiedTs = rs.getTimestamp("Modified_Date");

            return CustomerSelectDto.builder()
                    .id(rs.getInt("Id"))
                    .companyRefId(rs.getInt("CompanyRefId"))

                    .cNumberDisplay(rs.getString("CNumberDisplay"))
                    .cNumber(rs.getInt("CNumber"))
                    .customerName(rs.getString("CustomerName"))


                    .address1(rs.getString("Address1"))
                    .address2(rs.getString("Address2"))
                    .address3(rs.getString("Address3"))
                    .city(rs.getString("City"))
                    .state(rs.getString("State"))
                    .zipcode(rs.getString("Zipcode"))
                    .countryId(rs.getInt("countryId"))
                    .email(rs.getString("Email"))
                    .mobileNo(rs.getString("MobileNo"))
                    .userName(rs.getString("UserName"))
                    .password(rs.getString("Password"))
                    .latitude(rs.getString("Latitude"))
                    .longitude(rs.getString("longitude"))
                    .gstNo(rs.getString("GSTNO"))
                    .tinNo(rs.getString("TinNo"))
                    .sstNo(rs.getString("SSTNo"))
                    .tinType(rs.getString("Tintype"))
                    .customerTin(rs.getString("CustomerTin"))
                    .bankName(rs.getString("BankName"))
                    .accountNo(rs.getString("AccountNo"))
                    .active(rs.getInt("Active"))
                    .createdDate(createdTs != null ? createdTs.toLocalDateTime() : null)
                    .modifiedDate(modifiedTs != null ? modifiedTs.toLocalDateTime() : null)
                    .sName(rs.getString("SName"))
                    .termsName(rs.getString("TermsName"))
                    .accountCode(rs.getString("AccountCode"))
                    .cmName(rs.getString("CMName"))
                    .personId(rs.getString("PersonId"))
                    .tokenId(rs.getString("TokenId"))
                    .oEmail(rs.getString("OEmail"))
                    .oEmail1(rs.getString("OEmail1"))
                    .oName(rs.getString("OName"))
                    .oPhone(rs.getString("OPhone"))

                    .aEmail(rs.getString("AEmail"))
                    .aEmail1(rs.getString("AEmail1"))
                    .aName(rs.getString("AName"))
                    .aPhone(rs.getString("APhone"))

                    .companyCode(rs.getString("CompanyCode"))
                    .expiryDate(rs.getString("ExpiryDate"))
                    .updateId(rs.getString("UpdateId"))
                    .customerCity(rs.getString("CustomerCity"))

                    .serviceTaxType(rs.getString("ServiceTaxType"))
                    .msicCode(rs.getString("MsicCode"))
                    .registrationNo(rs.getString("RegistrationNo"))
                    .exemptionNo(rs.getString("ExemptionNo"))
                    .exemptionDetails(rs.getString("ExemptionDetails"))
                    .symbolRefId(rs.getInt("SymbolRefid"))
                    .paymentTermsRefId(rs.getInt("PaymentTermsRefid"))
                    .eInvoice(rs.getString("eInvoice"))
                    .build();
        });

    }
}
