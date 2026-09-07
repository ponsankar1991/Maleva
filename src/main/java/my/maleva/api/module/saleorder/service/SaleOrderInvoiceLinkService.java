package my.maleva.api.module.saleorder.service;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceLink;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Answers "has this sale order already been invoiced?" — the port of the
 * legacy {@code SaleOrderServices.SelectInvoiceNumber} that the Push Invoice
 * button called before opening the invoice screen.
 *
 * <p>Three corrections to that query, each one measured against the demo
 * database rather than assumed:
 *
 * <ul>
 *   <li><b>Company.</b> Legacy matched on the sale order id alone. On a
 *       multi-company database that reads another company's invoices.</li>
 *   <li><b>All three links.</b> Legacy looked only at
 *       {@code SaleDetails.SaleOrderMasterRefId}. A job can also be tied to
 *       an invoice through {@code SaleMasterReference} (multi-job invoices)
 *       or {@code SaleOrderMaster.InvoiceNo}. Counting company 6: 15,356 jobs
 *       are linked through the detail lines and the reference table, but only
 *       15,263 have {@code InvoiceNo} filled in — 93 jobs would look
 *       un-invoiced and could be billed twice if only that column were
 *       trusted.</li>
 *   <li><b>Deleted invoices.</b> Only {@code Active = 1} invoices count, so a
 *       job whose invoice was deleted can be billed again instead of being
 *       blocked for ever.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SaleOrderInvoiceLinkService {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * @return the link, or empty when no such sale order belongs to the company
     */
    @Transactional(readOnly = true)
    public Optional<SaleOrderInvoiceLink> find(Integer saleOrderId, Integer companyId) {
        if (saleOrderId == null || saleOrderId <= 0 || companyId == null || companyId <= 0) {
            return Optional.empty();
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("saleOrderId", saleOrderId)
                .addValue("companyId", companyId);

        List<String[]> job = jdbc.query("""
                SELECT TOP 1 ISNULL(S.CNumberDisplay, '') AS JobNo, ISNULL(C.CustomerName, '') AS CustomerName
                FROM SaleOrderMaster S WITH (NOLOCK)
                LEFT JOIN Customer C WITH (NOLOCK) ON C.Id = S.CustomerRefId
                WHERE S.Id = :saleOrderId AND S.CompanyRefId = :companyId
                """, params, (rs, i) -> new String[]{rs.getString("JobNo"), rs.getString("CustomerName")});
        if (job.isEmpty()) {
            return Optional.empty();
        }
        String jobNo = job.get(0)[0];
        String customerName = job.get(0)[1];

        List<SaleOrderInvoiceLink> invoices = jdbc.query("""
                SELECT TOP 1 SM.Id AS InvoiceId, ISNULL(SM.CNumberDisplay, '') AS InvoiceNo,
                       ISNULL(FORMAT(SM.SaleDate, 'dd/MM/yyyy'), '') AS InvoiceDate,
                       ISNULL(SM.QNECode, '') AS QNECode, ISNULL(SM.EInvoiceUid, '') AS EInvoiceUid
                FROM SaleMaster SM WITH (NOLOCK)
                WHERE SM.CompanyRefId = :companyId AND SM.Active = 1
                  AND (EXISTS (SELECT 1 FROM SaleDetails SD WITH (NOLOCK)
                               WHERE SD.SaleMasterRefId = SM.Id AND SD.SaleOrderMasterRefId = :saleOrderId)
                    OR EXISTS (SELECT 1 FROM SaleMasterReference R WITH (NOLOCK)
                               WHERE R.SaleMasterRefId = SM.Id AND R.SaleOrderMasterRefId = :saleOrderId)
                    OR EXISTS (SELECT 1 FROM SaleOrderMaster SO WITH (NOLOCK)
                               WHERE SO.Id = :saleOrderId AND SO.InvoiceNo = SM.Id))
                ORDER BY SM.Id DESC
                """, params, (rs, i) -> new SaleOrderInvoiceLink(
                        saleOrderId, jobNo, customerName, true,
                        rs.getInt("InvoiceId"),
                        rs.getString("InvoiceNo"),
                        rs.getString("InvoiceDate"),
                        rs.getString("QNECode"),
                        rs.getString("EInvoiceUid")));

        return Optional.of(invoices.isEmpty()
                ? SaleOrderInvoiceLink.notInvoiced(saleOrderId, jobNo, customerName)
                : invoices.get(0));
    }
}
