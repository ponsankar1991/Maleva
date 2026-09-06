package my.maleva.api.module.paymentrecept.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.paymentrecept.dto.ReceiptBillDto;
import my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReceiptBillQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Executes the CustomerBalance or CustomerBalance_Single table-valued function.
     * Maps the result to ReceiptBillDto.
     */
    public List<ReceiptBillDto> selectCustomerBalance(ReceiptViewBillRequest request) {
        String sql;
        MapSqlParameterSource params = new MapSqlParameterSource();
        
        // Handle nulls to match legacy C# behavior
        int companyRefId = request.getCompanyRefId() != null ? request.getCompanyRefId() : 0;
        String tilldate = request.getTilldate() == null ? "" : request.getTilldate();
        
        params.addValue("companyRefId", companyRefId);
        params.addValue("tilldate", tilldate);

        if (request.getId() != null && request.getId() != 0) {
            sql = "SELECT * FROM CustomerBalance_Single(:id, :companyRefId, :tilldate)";
            params.addValue("id", request.getId());
        } else {
            sql = "SELECT * FROM CustomerBalance(:companyRefId, :tilldate)";
        }

        log.debug("Executing selectCustomerBalance: {}", sql);
        
        return jdbc.query(sql, params, (rs, rowNum) -> {
            ReceiptBillDto dto = new ReceiptBillDto();
            dto.setCompanyRefId(rs.getObject("CompanyRefId") != null ? rs.getInt("CompanyRefId") : null);
            dto.setSdId(rs.getInt("SDId"));
            dto.setSdId1(rs.getInt("SDId1"));
            dto.setReceiptRefId(rs.getInt("ReceiptRefId"));
            dto.setAmount(rs.getBigDecimal("Amount"));
            dto.setSaleCreditMasterRefId(rs.getInt("SaleCreditMasterRefId"));
            dto.setSaleCreditAmount(rs.getBigDecimal("SaleCreditAmount"));
            dto.setId(rs.getObject("Id") != null ? rs.getInt("Id") : null);
            dto.setCustomerName(rs.getString("CustomerName"));
            dto.setSaleMasterRefId(rs.getObject("SaleMasterRefId") != null ? rs.getInt("SaleMasterRefId") : null);
            dto.setCustomeropenRefId(rs.getObject("CustomeropenRefId") != null ? rs.getInt("CustomeropenRefId") : null);
            dto.setBillNo(rs.getString("BillNo"));
            java.sql.Timestamp billDateTs = rs.getTimestamp("BillDate");
            dto.setBillDate(billDateTs != null ? billDateTs.toLocalDateTime() : null);
            dto.setSBillDate(rs.getString("SBillDate"));
            dto.setBillAmount(rs.getBigDecimal("BillAmount"));
            dto.setReceipt(rs.getBigDecimal("Receipt"));
            dto.setBalance(rs.getBigDecimal("Balance"));
            dto.setCurrencyValue(rs.getBigDecimal("CurrencyValue"));
            dto.setActualAmount(rs.getBigDecimal("ActualAmount"));
            return dto;
        });
    }

    /**
     * Port of legacy RT_CustomerBills stored procedure directly into Java / SQL.
     * Selects all outstanding credit sales bills and customer opening balance for a customer,
     * deducting any receipts (excluding excludeReceiptId) and credit notes.
     *
     * @param request Contains id (CustomerRefId), companyRefId, and id2 (exclude receipt id)
     * @return List of ReceiptBillDto
     */
    public List<ReceiptBillDto> selectCustomerBills(ReceiptViewBillRequest request) {
        int customerId = request.getId() != null ? request.getId() : 0;
        int companyRefId = request.getCompanyRefId() != null ? request.getCompanyRefId() : 0;
        int excludeReceiptId = request.getId2() != null ? request.getId2() : 0;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("customerId", customerId);
        params.addValue("companyRefId", companyRefId);
        params.addValue("excludeReceiptId", excludeReceiptId);

        String sql = "SELECT "
                + "t.SaleMasterRefId, "
                + "t.CustomeropenRefId, "
                + "t.BillNo, "
                + "t.BillDate, "
                + "CAST(t.BillAmount AS NUMERIC(18,2)) AS BillAmount, "
                + "CAST(t.Receipt AS NUMERIC(18,2)) AS Receipt, "
                + "CAST((t.BillAmount - t.Receipt) AS NUMERIC(18,2)) AS Balance, "
                + "FORMAT(ISNULL(t.BillDate, '1900-01-01'), 'dd/MM/yyyy') AS SBillDate "
                + "FROM ( "
                // 1. Credit Sales from SaleMaster
                + "    SELECT "
                + "        P.Id AS SaleMasterRefId, "
                + "        CAST(NULL AS INT) AS CustomeropenRefId, "
                + "        ISNULL(P.CNumberDisplay, '') AS BillNo, "
                + "        P.SaleDate AS BillDate, "
                + "        CAST(P.Amount AS NUMERIC(18,2)) AS BillAmount, "
                + "        ( "
                + "            ISNULL(( "
                + "                SELECT SUM(ISNULL(pd.ReceiptAmount, 0)) "
                + "                FROM ReceiptDetails pd WITH (NOLOCK) "
                + "                INNER JOIN Receipt py WITH (NOLOCK) ON py.Id = pd.ReceiptRefId "
                + "                WHERE pd.SaleMasterRefId = P.Id AND py.Id != :excludeReceiptId "
                + "            ), 0) "
                + "            + "
                + "            ISNULL(( "
                + "                SELECT SUM(ISNULL(B.SaleCreditAmount, 0)) "
                + "                FROM SaleCreditMaster A WITH (NOLOCK) "
                + "                INNER JOIN SaleCreditKnockOff B WITH (NOLOCK) ON A.Id = B.SaleCreditMasterRefId "
                + "                WHERE B.SaleMasterRefId = P.Id AND B.Id != :excludeReceiptId "
                + "            ), 0) "
                + "        ) AS Receipt "
                + "    FROM SaleMaster P WITH (NOLOCK) "
                + "    WHERE P.CompanyRefId = :companyRefId "
                + "      AND P.Active = 1 "
                + "      AND P.SaleType = 'CREDIT' "
                + "      AND P.CustomerRefId = :customerId "
                + " "
                + "    UNION ALL "
                + " "
                // 2. Customer Opening Balance from Customer
                + "    SELECT "
                + "        CAST(NULL AS INT) AS SaleMasterRefId, "
                + "        P.Id AS CustomeropenRefId, "
                + "        '' AS BillNo, "
                + "        CAST('1900-01-01' AS DATETIME) AS BillDate, "
                + "        CAST(P.OpeningBalance AS NUMERIC(18,2)) AS BillAmount, "
                + "        ( "
                + "            ISNULL(( "
                + "                SELECT SUM(ISNULL(pd.ReceiptAmount, 0)) "
                + "                FROM ReceiptDetails pd WITH (NOLOCK) "
                + "                INNER JOIN Receipt py WITH (NOLOCK) ON py.Id = pd.ReceiptRefId "
                + "                WHERE pd.CustomeropenRefId = P.Id AND py.Id != :excludeReceiptId "
                + "            ), 0) "
                + "            + "
                + "            ISNULL(( "
                + "                SELECT SUM(ISNULL(B.SaleCreditAmount, 0)) "
                + "                FROM SaleCreditMaster A WITH (NOLOCK) "
                + "                INNER JOIN SaleCreditKnockOff B WITH (NOLOCK) ON A.Id = B.SaleCreditMasterRefId "
                + "                WHERE B.CustomeropenRefId = P.Id AND B.Id != :excludeReceiptId "
                + "            ), 0) "
                + "        ) AS Receipt "
                + "    FROM Customer P WITH (NOLOCK) "
                + "    WHERE P.CompanyRefId = :companyRefId "
                + "      AND P.Active = 1 "
                + "      AND P.Id = :customerId "
                + ") t "
                + "WHERE CAST((t.BillAmount - t.Receipt) AS NUMERIC(18,2)) != 0 "
                + "ORDER BY t.BillDate";

        log.info("Executing selectCustomerBills: customerId={}, companyRefId={}, excludeReceiptId={}",
                customerId, companyRefId, excludeReceiptId);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            ReceiptBillDto dto = new ReceiptBillDto();
            dto.setCompanyRefId(companyRefId);
            dto.setSaleMasterRefId(rs.getObject("SaleMasterRefId") != null ? rs.getInt("SaleMasterRefId") : null);
            dto.setCustomeropenRefId(rs.getObject("CustomeropenRefId") != null ? rs.getInt("CustomeropenRefId") : null);
            dto.setBillNo(rs.getString("BillNo"));
            java.sql.Timestamp billDateTs = rs.getTimestamp("BillDate");
            dto.setBillDate(billDateTs != null ? billDateTs.toLocalDateTime() : null);
            dto.setBillAmount(rs.getBigDecimal("BillAmount"));
            dto.setReceipt(rs.getBigDecimal("Receipt"));
            dto.setBalance(rs.getBigDecimal("Balance"));
            dto.setSBillDate(rs.getString("SBillDate"));
            dto.setAmount(java.math.BigDecimal.ZERO);
            dto.setActualAmount(java.math.BigDecimal.ZERO);
            dto.setCurrencyValue(java.math.BigDecimal.ONE);
            dto.setSdId(0);
            dto.setSdId1(0);
            dto.setReceiptRefId(0);
            dto.setSaleCreditMasterRefId(0);
            dto.setSaleCreditAmount(java.math.BigDecimal.ZERO);
            return dto;
        });
    }
}
