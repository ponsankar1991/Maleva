package my.maleva.api.module.saleorder.service;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.saleorder.dto.DoConvertResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Prepares (or refreshes) the delivery order of a sale order — the port of
 * the legacy {@code SaleOrderServices.DoConvert}, which the Sale Invoice
 * view's DO button called with the invoice's sale order.
 *
 * <p>The work is done by the existing {@code SP_DoMaster}: it inserts a
 * DoMaster row copied from the sale order and numbers it from
 * SequenceNoMaster ('DoMaster'), or, when the sale order already has a DO,
 * re-copies the sale order's current values onto it. Either way the
 * procedure's own transaction commits it; nothing here needs a Spring
 * transaction and none is opened, so a failure inside the procedure cannot
 * leave the caller's transaction rollback-only.
 *
 * <p>Known trap in the procedure, handled here: its "not found" branch
 * concatenates the sale order id onto a varchar and fails with a conversion
 * error instead of the intended message, so that error is translated.
 */
@Service
@RequiredArgsConstructor
public class SaleOrderDoConvertService {

    private static final Logger log = LoggerFactory.getLogger(SaleOrderDoConvertService.class);

    private final NamedParameterJdbcTemplate jdbc;

    public DoConvertResult convert(Integer saleOrderId, Integer companyId) {
        if (saleOrderId == null || saleOrderId <= 0) {
            return DoConvertResult.failure("This invoice has no sale order to prepare a DO from");
        }
        if (companyId == null || companyId <= 0) {
            return DoConvertResult.failure("Company is required");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("soId", saleOrderId)
                .addValue("comid", companyId);

        SpResult sp;
        try {
            List<SpResult> results = jdbc.query("EXEC [SP_DoMaster] :soId, :comid", params,
                    (rs, i) -> new SpResult(rs.getInt("Result"), rs.getString("msg"), rs.getInt("id"), rs.getString("BillNo")));
            if (results.isEmpty()) {
                return DoConvertResult.failure("SP_DoMaster returned nothing for sale order " + saleOrderId);
            }
            sp = results.get(0);
        } catch (DataAccessException ex) {
            String reason = rootMessage(ex);
            log.warn("SP_DoMaster failed for sale order {} company {}: {}", saleOrderId, companyId, reason);
            if (reason.contains("Conversion failed")) {
                // the procedure's own not-found message never survives its varchar + int concat
                return DoConvertResult.failure("Sale order " + saleOrderId + " was not found for this company");
            }
            return DoConvertResult.failure("DO could not be prepared: " + reason);
        }

        if (sp.result() != 1) {
            return DoConvertResult.failure(sp.msg() == null || sp.msg().isBlank() ? "DO could not be prepared" : sp.msg());
        }

        Integer doId = sp.id() > 0 ? sp.id() : existingDoId(saleOrderId, companyId);
        if (doId == null || doId <= 0) {
            return DoConvertResult.failure("DO was prepared but its number could not be read back");
        }

        List<DoConvertResult.DoView> rows = jdbc.query("""
                SELECT So.AWBNo, So.BLCopy, c.CustomerName, c.Address1 AS Address, So.DODescription,
                       c.City AS AttnName, Do.CNumberDisplay AS DoNo, So.CNumberDisplay AS JobNo,
                       FORMAT(Do.SaleDate, 'dd/MM/yyyy') AS SaleDate,
                       So.Offvesselname, So.Loadingvesselname, J.Name AS JobName, So.TotalWeight, So.Quantity
                FROM DoMaster Do WITH (NOLOCK)
                INNER JOIN SaleOrderMaster So WITH (NOLOCK) ON So.DOCNo = Do.Id
                INNER JOIN Customer c WITH (NOLOCK) ON c.Id = So.CustomerRefId
                INNER JOIN JobTypeMaster J WITH (NOLOCK) ON J.Id = So.JobMasterRefId
                WHERE Do.Id = :doId AND Do.CompanyRefId = :comid
                ORDER BY Do.SaleDate
                """,
                new MapSqlParameterSource().addValue("doId", doId).addValue("comid", companyId),
                (rs, i) -> DoConvertResult.DoView.builder()
                        .doNo(rs.getString("DoNo"))
                        .jobNo(rs.getString("JobNo"))
                        .saleDate(rs.getString("SaleDate"))
                        .customerName(rs.getString("CustomerName"))
                        .address(rs.getString("Address"))
                        .attnName(rs.getString("AttnName"))
                        .doDescription(rs.getString("DODescription"))
                        .jobName(rs.getString("JobName"))
                        .awbNo(rs.getString("AWBNo"))
                        .blCopy(rs.getString("BLCopy"))
                        .offVesselName(rs.getString("Offvesselname"))
                        .loadingVesselName(rs.getString("Loadingvesselname"))
                        .totalWeight(rs.getString("TotalWeight"))
                        .quantity(rs.getString("Quantity"))
                        .build());

        if (rows.isEmpty()) {
            return DoConvertResult.failure("DO " + doId + " was prepared but has no printable rows");
        }
        String doNo = rows.get(0).getDoNo() != null ? rows.get(0).getDoNo() : sp.billNo();
        return new DoConvertResult(true, "DO " + doNo + " prepared", doId, doNo, rows);
    }

    private Integer existingDoId(Integer saleOrderId, Integer companyId) {
        List<Integer> ids = jdbc.query(
                "SELECT ISNULL(DocNo, 0) AS DocNo FROM SaleOrderMaster WITH (NOLOCK) WHERE Id = :soId AND CompanyRefId = :comid",
                new MapSqlParameterSource().addValue("soId", saleOrderId).addValue("comid", companyId),
                (rs, i) -> rs.getInt("DocNo"));
        return ids.isEmpty() ? null : ids.get(0);
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() == null ? ex.getClass().getSimpleName() : t.getMessage();
    }

    /** The first result set of SP_DoMaster. */
    record SpResult(int result, String msg, int id, String billNo) {
    }
}
