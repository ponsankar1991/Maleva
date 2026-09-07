package my.maleva.api.module.salecreditmaster.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditBillDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * What a customer still owes, for the credit note's knock-off grid — the port
 * of {@code RT_CustomerBills} as the Sale Credit screen needs it.
 *
 * <p>One row per credit invoice plus one for the customer's opening balance,
 * each with what receipts and other credit notes have already taken off it.
 * Rows that are fully settled are dropped, as legacy did.
 *
 * <p>The difference from the receipt screen's copy is the exclusion. Legacy
 * passed the open document's id into the procedure and it was compared to
 * {@code SaleCreditKnockOff.Id} — a knock-off LINE id. So opening a credit
 * note for edit excluded some unrelated line (or nothing at all) instead of
 * the note's own knock-offs, and the balances it was built from came back
 * already reduced by itself. Here the exclusion is
 * {@code SaleCreditKnockOff.SaleCreditMasterRefId <> :excludeCreditNoteId},
 * which is the credit note being edited.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SaleCreditBillQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private static final String SQL = """
            SELECT t.SaleMasterRefId,
                   t.CustomeropenRefId,
                   t.BillNo,
                   CASE WHEN t.BillDate = '1900-01-01' THEN ''
                        ELSE FORMAT(t.BillDate, 'dd/MM/yyyy') END AS SBillDate,
                   CAST(t.BillAmount AS NUMERIC(18,2))                  AS BillAmount,
                   CAST(t.Settled AS NUMERIC(18,2))                     AS Settled,
                   CAST((t.BillAmount - t.Settled) AS NUMERIC(18,2))    AS Balance
            FROM (
                SELECT P.Id                                AS SaleMasterRefId,
                       CAST(NULL AS INT)                   AS CustomeropenRefId,
                       ISNULL(P.CNumberDisplay, '')        AS BillNo,
                       P.SaleDate                          AS BillDate,
                       CAST(P.Amount AS NUMERIC(18,2))     AS BillAmount,
                       ISNULL((SELECT SUM(ISNULL(RD.ReceiptAmount, 0))
                               FROM ReceiptDetails RD WITH (NOLOCK)
                               WHERE RD.SaleMasterRefId = P.Id), 0)
                     + ISNULL((SELECT SUM(ISNULL(KO.SaleCreditAmount, 0))
                               FROM SaleCreditKnockOff KO WITH (NOLOCK)
                               WHERE KO.SaleMasterRefId = P.Id
                                 AND KO.SaleCreditMasterRefId <> :excludeCreditNoteId), 0) AS Settled
                FROM SaleMaster P WITH (NOLOCK)
                WHERE P.CompanyRefId = :companyId
                  AND P.Active = 1
                  AND P.SaleType = 'CREDIT'
                  AND P.CustomerRefId = :customerId

                UNION ALL

                SELECT CAST(NULL AS INT)                   AS SaleMasterRefId,
                       C.Id                                AS CustomeropenRefId,
                       ''                                  AS BillNo,
                       CAST('1900-01-01' AS DATETIME)      AS BillDate,
                       CAST(C.OpeningBalance AS NUMERIC(18,2)) AS BillAmount,
                       ISNULL((SELECT SUM(ISNULL(RD.ReceiptAmount, 0))
                               FROM ReceiptDetails RD WITH (NOLOCK)
                               WHERE RD.CustomeropenRefId = C.Id), 0)
                     + ISNULL((SELECT SUM(ISNULL(KO.SaleCreditAmount, 0))
                               FROM SaleCreditKnockOff KO WITH (NOLOCK)
                               WHERE KO.CustomeropenRefId = C.Id
                                 AND KO.SaleCreditMasterRefId <> :excludeCreditNoteId), 0) AS Settled
                FROM Customer C WITH (NOLOCK)
                WHERE C.CompanyRefId = :companyId
                  AND C.Active = 1
                  AND C.Id = :customerId
            ) t
            WHERE CAST((t.BillAmount - t.Settled) AS NUMERIC(18,2)) <> 0
            ORDER BY t.BillDate, t.BillNo
            """;

    /**
     * @param excludeCreditNoteId the credit note open in the screen, so the
     *                            documents it already settles read as
     *                            outstanding again; 0 for a new credit note
     */
    public List<SaleCreditBillDto> selectCustomerBills(Integer companyId, Integer customerId, Integer excludeCreditNoteId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId == null ? 0 : companyId)
                .addValue("customerId", customerId == null ? 0 : customerId)
                .addValue("excludeCreditNoteId", excludeCreditNoteId == null ? 0 : excludeCreditNoteId);

        return jdbc.query(SQL, params, (rs, row) -> SaleCreditBillDto.builder()
                .saleMasterRefId(rs.getObject("SaleMasterRefId") == null ? null : rs.getInt("SaleMasterRefId"))
                .customeropenRefId(rs.getObject("CustomeropenRefId") == null ? null : rs.getInt("CustomeropenRefId"))
                .billNo(rs.getString("BillNo"))
                .sBillDate(rs.getString("SBillDate"))
                .billAmount(rs.getBigDecimal("BillAmount"))
                .settled(rs.getBigDecimal("Settled"))
                .balance(rs.getBigDecimal("Balance"))
                .saleCreditAmount(BigDecimal.ZERO)
                .currencyValue(BigDecimal.ONE)
                .actualAmount(BigDecimal.ZERO)
                .build());
    }
}
