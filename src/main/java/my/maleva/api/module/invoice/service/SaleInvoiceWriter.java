package my.maleva.api.module.invoice.service;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceDetailRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceRequestDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Writes an invoice in Java, the port of {@code SP_SaleMaster}'s insert and
 * update branches.
 *
 * <p>The procedure serialises the whole invoice to JSON, shreds it back with
 * OPENJSON into a temp table, loops over that table one row at a time and
 * re-declares a hundred local variables to do a single INSERT. This does the
 * same writes directly: no JSON round trip, no temp table, no cursor loop.
 *
 * <p><b>Behaviour is copied, not improved.</b> The .NET screens still call the
 * procedure against the same tables, so an invoice written here must be
 * indistinguishable from one it wrote. Each quirk below is deliberate:
 *
 * <ul>
 *   <li><b>A reference id of 0 is stored as NULL.</b> The procedure's
 *       {@code if @UserRefId = ''} blocks look like empty-string tests but
 *       these are int variables, and T-SQL converts {@code ''} to 0 — so what
 *       they really mean is "0 becomes NULL". {@link #refId} is that rule.</li>
 *   <li><b>SaleType is always 'CREDIT'</b>, whatever the request says.</li>
 *   <li><b>An edit does not touch DOCNo, CNumber, CNumberDisplay, Active or
 *       the Created_/Modified_ audit columns</b> — the procedure comments the
 *       first three out and never lists the rest. Copied as-is; changing what
 *       an edit stamps is a decision for the business, not a port.</li>
 *   <li><b>An edit sets LastEmployeeRefId but not EmployeeRefId</b>, so the
 *       original raiser survives and the last editor is recorded separately.</li>
 *   <li><b>An edit clears and rewrites</b> the lines, the references, and the
 *       InvoiceNo stamp on every sale order that pointed at this invoice.</li>
 * </ul>
 *
 * <p>Numbering is not here. It stays in {@link SaleInvoiceTransactionService},
 * which seeds a missing SequenceNoMaster row before allocating — without that
 * seed the procedure's {@code UPDATE SequenceNoMaster} matched nothing and
 * every invoice for the company came out numbered 1.
 */
@Component
@RequiredArgsConstructor
public class SaleInvoiceWriter {

    private static final String SALE_TYPE = "CREDIT";
    private static final int ACTIVE = 1;

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * The database login, read once.
     *
     * <p>Every save used to spend a round trip on {@code SELECT SUSER_NAME()}
     * for a value that cannot change: the application connects as one user for
     * the life of the process.
     */
    private volatile String auditUser;

    /** The columns an insert fills, in the procedure's own order. */
    private static final List<String> INSERT_COLUMNS = List.of(
            "CompanyRefId", "UserRefId", "BillType", "EmployeeRefId", "LastEmployeeRefId",
            "CustomerRefId", "JobMasterRefId", "AgentCompanyRefId", "AgentMasterRefId",
            "OAgentCompanyRefId", "OAgentMasterRefId", "SaleDate", "SaleType", "Coinage",
            "GrossAmount", "TaxAmount", "DiscountAmount", "Remarks", "Remarks1", "DODescription",
            "PlusAmount", "MinusAmount", "Amount", "Active", "Created_Date", "Created_By",
            "Modified_Date", "Modified_By", "Offvesselname", "Loadingvesselname", "TruckSize",
            "SPort", "OPort", "SCN", "ETA", "ETB", "ETD", "OETA", "OETB", "OETD", "DOCNo",
            "SaleOrderMasterNo", "TruckRefid", "DriverRefid", "AWBNo", "BLCopy", "Quantity",
            "TotalWeight", "Vessel", "OVessel", "Commodity", "Cargo", "JStatus", "OStatus",
            "ForkliftbyRefid", "SealbyRefid", "SealbreakbyRefid", "BoardingOfficerRefid",
            "BoardingOfficer1Refid", "BoardingAmount", "BoardingAmount1", "ForwardingEnterRef",
            "ForwardingExitRef", "PortChargesRef", "PortCharges", "SealAmount", "BreakSealAmount",
            "ForwardingEnterRef2", "ForwardingExitRef2", "ForwardingEnterRef3", "ForwardingExitRef3",
            "Forwarding2", "Forwarding3", "ForwardingSMKNo", "ForwardingSMKNo2", "ForwardingSMKNo3",
            "Zb2", "ZbRef", "ZbRef2", "SealAmount2", "BreakSealAmount2", "SealAmount3",
            "BreakSealAmount3", "SealbyRefid2", "SealbreakbyRefid2", "SealbyRefid3",
            "SealbreakbyRefid3", "LSCN", "PickupDate", "DeliveryDate", "WareHouseEnterDate",
            "WareHouseExitDate", "PickupAddress", "DeliveryAddress", "WareHouseAddress",
            "Forwarding", "Origin", "Destination", "Zb", "PTW", "CNumberDisplay", "CNumber",
            "CurrencyValue", "ActualNetAmount", "SymbolRefId");

    /**
     * The columns an edit rewrites. Deliberately absent, matching the
     * procedure: DOCNo, CNumber, CNumberDisplay, EmployeeRefId, Active,
     * Created_Date/By, Modified_Date/By, CompanyRefId.
     */
    private static final List<String> UPDATE_COLUMNS = List.of(
            "CustomerRefId", "LastEmployeeRefId", "UserRefId", "JobMasterRefId",
            "AgentCompanyRefId", "AgentMasterRefId", "OAgentCompanyRefId", "OAgentMasterRefId",
            "SaleDate", "SaleType", "GrossAmount", "TaxAmount", "DiscountAmount", "Remarks",
            "Remarks1", "DODescription", "PlusAmount", "MinusAmount", "Coinage", "Amount",
            "Offvesselname", "Loadingvesselname", "TruckSize", "SPort", "OPort", "SCN",
            "ETA", "ETB", "ETD", "OETA", "OETB", "OETD", "SaleOrderMasterNo", "TruckRefid",
            "DriverRefid", "AWBNo", "BLCopy", "Quantity", "TotalWeight", "Vessel", "OVessel",
            "Commodity", "Cargo", "JStatus", "OStatus", "ForkliftbyRefid", "SealbyRefid",
            "SealbreakbyRefid", "BoardingOfficerRefid", "BoardingOfficer1Refid", "BoardingAmount",
            "BoardingAmount1", "ForwardingEnterRef", "ForwardingExitRef", "PortChargesRef",
            "PortCharges", "SealAmount", "BreakSealAmount", "ForwardingEnterRef2",
            "ForwardingExitRef2", "ForwardingEnterRef3", "ForwardingExitRef3", "Forwarding2",
            "Forwarding3", "ForwardingSMKNo", "ForwardingSMKNo2", "ForwardingSMKNo3", "Zb2",
            "ZbRef", "ZbRef2", "SealAmount2", "BreakSealAmount2", "SealAmount3", "BreakSealAmount3",
            "SealbyRefid2", "SealbreakbyRefid2", "SealbyRefid3", "SealbreakbyRefid3", "LSCN",
            "PickupDate", "DeliveryDate", "WareHouseEnterDate", "WareHouseExitDate",
            "WareHouseAddress", "PickupAddress", "DeliveryAddress", "Forwarding", "Origin",
            "Destination", "Zb", "PTW", "SymbolRefId", "CurrencyValue", "ActualNetAmount");

    /**
     * Creates or rewrites the invoice and everything hanging off it.
     *
     * <p>Must run inside the caller's transaction: the header, its lines, the
     * references and the sale-order stamps are one change and must fail as one.
     *
     * @return the invoice id — the new identity on a create, the same id on an edit
     */
    public Integer write(SaleInvoiceRequestDTO request, List<SaleInvoiceDetailRequestDTO> lines) {
        Integer companyId = request.getCompanyRefId();
        boolean creating = request.getId() == null || request.getId() == 0;

        Integer invoiceId;
        if (creating) {
            invoiceId = insertHeader(request);
            // The procedure stamps the single sale order named on the header
            // separately from the reference list below; a job can arrive by
            // either route and both must mark the order as invoiced.
            stampSaleOrder(request.getSaleOrderMasterNo(), invoiceId, companyId);
        } else {
            invoiceId = request.getId();
            requireEditTarget(invoiceId, companyId, request.getCNumberDisplay());
            clearPreviousVersion(invoiceId, companyId);
            updateHeader(request, invoiceId);
        }

        insertLines(invoiceId, lines);
        writeReferences(invoiceId, companyId, referencedSaleOrders(request, lines));
        return invoiceId;
    }

    // ──────────────────────────────────────────────────────────── header ──

    private Integer insertHeader(SaleInvoiceRequestDTO r) {
        String sql = "INSERT INTO SaleMaster (" + String.join(", ", bracketed(INSERT_COLUMNS)) + ") VALUES ("
                + String.join(", ", named(INSERT_COLUMNS)) + ")";

        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(sql, headerParameters(r, true), keys, new String[]{"Id"});

        Number id = keys.getKey();
        if (id == null) {
            // Belt and braces: SET NOCOUNT ON is on for this datasource, so a
            // silent failure here would otherwise surface much later as a
            // detail row pointing at invoice 0.
            throw new IllegalStateException("The invoice was inserted but no id came back");
        }
        return id.intValue();
    }

    private void updateHeader(SaleInvoiceRequestDTO r, Integer invoiceId) {
        List<String> assignments = new ArrayList<>(UPDATE_COLUMNS.size());
        for (String column : UPDATE_COLUMNS) {
            assignments.add("[" + column + "] = :" + column);
        }
        MapSqlParameterSource params = headerParameters(r, false);
        params.addValue("Id", invoiceId);

        jdbc.update("UPDATE SaleMaster SET " + String.join(", ", assignments) + " WHERE Id = :Id", params);
    }

    /**
     * Every header column as a bound parameter.
     *
     * <p>One map serves both statements; the update simply names fewer of
     * them. That keeps the two branches from drifting apart, which is exactly
     * what happened in the procedure — its insert and update lists disagree on
     * several columns and only a careful read reveals which.
     */
    private MapSqlParameterSource headerParameters(SaleInvoiceRequestDTO r, boolean creating) {
        MapSqlParameterSource p = new MapSqlParameterSource();

        p.addValue("CompanyRefId", r.getCompanyRefId());
        p.addValue("CustomerRefId", r.getCustomerRefId());
        p.addValue("JobMasterRefId", r.getJobMasterRefId());
        p.addValue("UserRefId", refId(r.getUserRefId()));
        p.addValue("EmployeeRefId", refId(r.getEmployeeRefId()));
        p.addValue("LastEmployeeRefId", refId(r.getEmployeeRefId()));
        p.addValue("AgentCompanyRefId", refId(r.getAgentCompanyRefId()));
        p.addValue("AgentMasterRefId", refId(r.getAgentMasterRefId()));
        p.addValue("OAgentCompanyRefId", refId(r.getOAgentCompanyRefId()));
        p.addValue("OAgentMasterRefId", refId(r.getOAgentMasterRefId()));

        p.addValue("BillType", text(r.getBillType()));
        p.addValue("SaleType", SALE_TYPE);
        p.addValue("SaleDate", date(r.getSaleDate()));

        p.addValue("GrossAmount", money(r.getGrossAmount()));
        p.addValue("TaxAmount", money(r.getTaxAmount()));
        p.addValue("DiscountAmount", money(r.getDiscountAmount()));
        p.addValue("PlusAmount", money(r.getPlusAmount()));
        p.addValue("MinusAmount", money(r.getMinusAmount()));
        p.addValue("Coinage", money(r.getCoinage()));
        p.addValue("Amount", money(r.getAmount()));
        p.addValue("CurrencyValue", money(r.getCurrencyValue()));
        p.addValue("ActualNetAmount", money(r.getActualNetAmount()));
        p.addValue("SymbolRefId", refId(r.getSymbolRefId()));

        p.addValue("Remarks", text(r.getRemarks()));
        p.addValue("Remarks1", text(r.getRemarks1()));
        p.addValue("DODescription", text(r.getDoDescription()));
        p.addValue("Offvesselname", text(r.getOffVesselName()));
        p.addValue("Loadingvesselname", text(r.getLoadingVesselName()));
        p.addValue("TruckSize", text(r.getTruckSize()));
        p.addValue("SPort", text(r.getSPort()));
        p.addValue("OPort", text(r.getOPort()));
        p.addValue("SCN", text(r.getScn()));
        p.addValue("LSCN", text(r.getLscn()));
        p.addValue("Vessel", text(r.getVessel()));
        p.addValue("OVessel", text(r.getOVessel()));
        p.addValue("Commodity", text(r.getCommodity()));
        p.addValue("Cargo", text(r.getCargo()));
        p.addValue("AWBNo", text(r.getAwbNo()));
        p.addValue("BLCopy", text(r.getBlCopy()));
        p.addValue("Quantity", text(r.getQuantity()));
        p.addValue("TotalWeight", text(r.getTotalWeight()));
        p.addValue("PTW", text(r.getPtw()));
        p.addValue("Origin", text(r.getOrigin()));
        p.addValue("Destination", text(r.getDestination()));

        p.addValue("ETA", dateTime(r.getEta()));
        p.addValue("ETB", dateTime(r.getEtb()));
        p.addValue("ETD", dateTime(r.getEtd()));
        p.addValue("OETA", dateTime(r.getOEta()));
        p.addValue("OETB", dateTime(r.getOEtb()));
        p.addValue("OETD", dateTime(r.getOEtd()));
        p.addValue("PickupDate", dateTime(r.getPickupDate()));
        p.addValue("DeliveryDate", dateTime(r.getDeliveryDate()));
        p.addValue("WareHouseEnterDate", dateTime(r.getWareHouseEnterDate()));
        p.addValue("WareHouseExitDate", dateTime(r.getWareHouseExitDate()));
        p.addValue("PickupAddress", text(r.getPickupAddress()));
        p.addValue("DeliveryAddress", text(r.getDeliveryAddress()));
        p.addValue("WareHouseAddress", text(r.getWareHouseAddress()));

        p.addValue("DOCNo", refId(r.getDocNo()));
        p.addValue("SaleOrderMasterNo", refId(r.getSaleOrderMasterNo()));
        p.addValue("TruckRefid", refId(r.getTruckRefId()));
        p.addValue("DriverRefid", refId(r.getDriverRefId()));
        p.addValue("JStatus", refId(r.getJStatus()));
        p.addValue("OStatus", zeroIfNull(r.getOStatus()));
        p.addValue("ForkliftbyRefid", refId(r.getForkliftByRefId()));
        p.addValue("SealbyRefid", refId(r.getSealByRefId()));
        p.addValue("SealbreakbyRefid", refId(r.getSealBreakByRefId()));
        p.addValue("SealbyRefid2", refId(r.getSealByRefId2()));
        p.addValue("SealbreakbyRefid2", refId(r.getSealBreakByRefId2()));
        p.addValue("SealbyRefid3", refId(r.getSealByRefId3()));
        p.addValue("SealbreakbyRefid3", refId(r.getSealBreakByRefId3()));
        p.addValue("BoardingOfficerRefid", refId(r.getBoardingOfficerRefId()));
        p.addValue("BoardingOfficer1Refid", refId(r.getBoardingOfficer1RefId()));
        p.addValue("BoardingAmount", money(r.getBoardingAmount()));
        p.addValue("BoardingAmount1", money(r.getBoardingAmount1()));

        p.addValue("Forwarding", text(r.getForwarding()));
        p.addValue("Forwarding2", text(r.getForwarding2()));
        p.addValue("Forwarding3", text(r.getForwarding3()));
        p.addValue("ForwardingEnterRef", text(r.getForwardingEnterRef()));
        p.addValue("ForwardingExitRef", text(r.getForwardingExitRef()));
        p.addValue("ForwardingEnterRef2", text(r.getForwardingEnterRef2()));
        p.addValue("ForwardingExitRef2", text(r.getForwardingExitRef2()));
        p.addValue("ForwardingEnterRef3", text(r.getForwardingEnterRef3()));
        p.addValue("ForwardingExitRef3", text(r.getForwardingExitRef3()));
        p.addValue("ForwardingSMKNo", text(r.getForwardingSmkNo()));
        p.addValue("ForwardingSMKNo2", text(r.getForwardingSmkNo2()));
        p.addValue("ForwardingSMKNo3", text(r.getForwardingSmkNo3()));

        p.addValue("PortChargesRef", text(r.getPortChargesRef()));
        p.addValue("PortCharges", money(r.getPortCharges()));
        p.addValue("SealAmount", money(r.getSealAmount()));
        p.addValue("BreakSealAmount", money(r.getBreakSealAmount()));
        p.addValue("SealAmount2", money(r.getSealAmount2()));
        p.addValue("BreakSealAmount2", money(r.getBreakSealAmount2()));
        p.addValue("SealAmount3", money(r.getSealAmount3()));
        p.addValue("BreakSealAmount3", money(r.getBreakSealAmount3()));

        p.addValue("Zb", text(r.getZb()));
        p.addValue("Zb2", text(r.getZb2()));
        p.addValue("ZbRef", text(r.getZbRef()));
        p.addValue("ZbRef2", text(r.getZbRef2()));

        if (creating) {
            // The number is allocated after the row exists, as the procedure
            // does — it needs the identity to update.
            p.addValue("CNumberDisplay", "");
            p.addValue("CNumber", 0);
            p.addValue("Active", ACTIVE);
            p.addValue("Created_Date", LocalDateTime.now());
            p.addValue("Modified_Date", LocalDateTime.now());
            p.addValue("Created_By", auditUser());
            p.addValue("Modified_By", auditUser());
        }
        return p;
    }

    // ─────────────────────────────────────────────────── edit house-keeping ──

    /**
     * Refuses an edit whose id is not an invoice of this company, or not the
     * invoice the screen says it is saving - before anything is deleted.
     *
     * <p>Nothing else would notice. SET NOCOUNT ON makes every UPDATE report -1
     * rows, so rewriting a header that does not exist looks exactly like
     * rewriting one that does; the first thing to object was the line insert's
     * FK_SaleDetails_SaleMaster, as a 500 (2026-09-17). That was the lucky
     * case. The screen had copied a SALE ORDER's id into the invoice id when a
     * job was added, and had that number also been a real invoice id, this
     * method's absence meant the save would have rewritten a different invoice,
     * deleted its lines and released its jobs - silently.
     *
     * <p>UPDLOCK also holds the row for the rest of the transaction, so two
     * edits of one invoice cannot interleave their delete-and-reinsert.
     *
     * @param expectedNumber the invoice number the screen showed, when it sent
     *     one; a mismatch means the id and the form disagree about which invoice
     *     this is
     */
    private void requireEditTarget(Integer invoiceId, Integer companyId, String expectedNumber) {
        List<String> numbers = jdbc.query(
                "SELECT ISNULL(CNumberDisplay, '') AS InvoiceNo FROM SaleMaster WITH (UPDLOCK, ROWLOCK) "
                        + "WHERE Id = :invoiceId AND CompanyRefId = :companyId",
                new MapSqlParameterSource()
                        .addValue("invoiceId", invoiceId)
                        .addValue("companyId", companyId),
                (rs, rowNum) -> rs.getString("InvoiceNo"));
        if (numbers.isEmpty()) {
            throw new InvalidRequestException("Invoice id " + invoiceId
                    + " does not exist for this company, so there is nothing to update. Nothing was saved."
                    + " Clear the form and open the invoice again from the list.");
        }
        String stored = numbers.get(0) == null ? "" : numbers.get(0).trim();
        String expected = expectedNumber == null ? "" : expectedNumber.trim();
        if (!expected.isEmpty() && !"0".equals(expected) && !expected.equalsIgnoreCase(stored)) {
            throw new InvalidRequestException("This save is for invoice " + expected
                    + " but its id points at invoice " + (stored.isEmpty() ? "#" + invoiceId : stored)
                    + ". Nothing was saved. Clear the form and open the invoice again from the list.");
        }
    }

    /**
     * Undoes what the previous version of this invoice claimed.
     *
     * <p>Order matters and is the procedure's: release the sale orders first,
     * then drop the reference rows and the lines, so the rewrite below starts
     * from nothing. An edit that dropped a job must leave that job free to be
     * invoiced again.
     */
    private void clearPreviousVersion(Integer invoiceId, Integer companyId) {
        SqlParameterSource scope = new MapSqlParameterSource()
                .addValue("invoiceId", invoiceId)
                .addValue("companyId", companyId);

        jdbc.update("UPDATE SaleOrderMaster SET InvoiceNo = 0 "
                + "WHERE InvoiceNo = :invoiceId AND CompanyRefId = :companyId", scope);
        jdbc.update("DELETE FROM SaleMasterReference WHERE SaleMasterRefId = :invoiceId", scope);
        jdbc.update("DELETE FROM SaleDetails WHERE SaleMasterRefId = :invoiceId", scope);
    }

    // ───────────────────────────────────────────────────────────── lines ──

    private void insertLines(Integer invoiceId, List<SaleInvoiceDetailRequestDTO> lines) {
        if (lines.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO SaleDetails (SaleMasterRefId, ItemMasterRefId, MRP, PurchaseRate, "
                + "ItemQty, DiscPer, DiscAmount, LandingCost, TaxPercent, TaxAmount, SalesRate, "
                + "NetSalesRate, Amount, Created_Date, Modified_Date, CurrencyValue, TaxRefId, "
                + "ActualAmount, SDRemarks, SaleOrderMasterRefId) VALUES ("
                + ":saleMasterRefId, :itemMasterRefId, :mrp, :purchaseRate, :itemQty, :discPer, "
                + ":discAmount, :landingCost, :taxPercent, :taxAmount, :salesRate, :netSalesRate, "
                + ":amount, :createdDate, :modifiedDate, :currencyValue, :taxRefId, :actualAmount, "
                + ":sdRemarks, :saleOrderMasterRefId)";

        LocalDateTime now = LocalDateTime.now();
        MapSqlParameterSource[] batch = new MapSqlParameterSource[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            SaleInvoiceDetailRequestDTO line = lines.get(i);
            batch[i] = new MapSqlParameterSource()
                    .addValue("saleMasterRefId", invoiceId)
                    .addValue("itemMasterRefId", line.getItemMasterRefId())
                    .addValue("mrp", money(line.getMrp()))
                    .addValue("purchaseRate", money(line.getPurchaseRate()))
                    .addValue("itemQty", money(line.getItemQty()))
                    .addValue("discPer", money(line.getDiscountPercent()))
                    .addValue("discAmount", money(line.getDiscountAmount()))
                    .addValue("landingCost", money(line.getLandingCost()))
                    .addValue("taxPercent", money(line.getTaxPercent()))
                    .addValue("taxAmount", money(line.getTaxAmount()))
                    .addValue("salesRate", money(line.getSalesRate()))
                    .addValue("netSalesRate", money(line.getNetSalesRate()))
                    .addValue("amount", money(line.getAmount()))
                    .addValue("createdDate", now)
                    .addValue("modifiedDate", now)
                    .addValue("currencyValue", money(line.getCurrencyValue()))
                    .addValue("taxRefId", refId(line.getTaxRefId()))
                    .addValue("actualAmount", money(line.getActualAmount()))
                    .addValue("sdRemarks", text(line.getRemarks()))
                    .addValue("saleOrderMasterRefId", refId(line.getSaleOrderMasterRefId()));
        }
        // One round trip for every line, instead of the procedure's
        // row-by-row OPENJSON insert.
        jdbc.batchUpdate(sql, batch);
    }

    // ──────────────────────────────────────────────────────── references ──

    /**
     * The sale orders this invoice bills. Taken from the request when it names
     * them, otherwise from the lines — the two must agree, because this list
     * alone decides which orders get stamped as invoiced.
     */
    static List<Integer> referencedSaleOrders(SaleInvoiceRequestDTO r, List<SaleInvoiceDetailRequestDTO> lines) {
        List<Integer> ids = new ArrayList<>();
        if (r.getSaleOrderRefIds() != null) {
            ids.addAll(r.getSaleOrderRefIds());
        }
        if (ids.isEmpty()) {
            lines.stream()
                    .map(SaleInvoiceDetailRequestDTO::getSaleOrderMasterRefId)
                    .filter(Objects::nonNull)
                    .forEach(ids::add);
        }
        List<Integer> distinct = new ArrayList<>(new LinkedHashSet<>(ids));
        distinct.removeIf(id -> id == null || id == 0);
        return distinct;
    }

    private void writeReferences(Integer invoiceId, Integer companyId, List<Integer> saleOrderIds) {
        if (saleOrderIds.isEmpty()) {
            return;
        }
        jdbc.update("UPDATE SaleOrderMaster SET InvoiceNo = :invoiceId "
                        + "WHERE Id IN (:ids) AND CompanyRefId = :companyId",
                new MapSqlParameterSource()
                        .addValue("invoiceId", invoiceId)
                        .addValue("ids", saleOrderIds)
                        .addValue("companyId", companyId));

        MapSqlParameterSource[] batch = new MapSqlParameterSource[saleOrderIds.size()];
        for (int i = 0; i < saleOrderIds.size(); i++) {
            batch[i] = new MapSqlParameterSource()
                    .addValue("saleMasterRefId", invoiceId)
                    .addValue("saleOrderMasterRefId", saleOrderIds.get(i));
        }
        jdbc.batchUpdate("INSERT INTO SaleMasterReference (SaleMasterRefId, SaleOrderMasterRefId) "
                + "VALUES (:saleMasterRefId, :saleOrderMasterRefId)", batch);
    }

    /** The single sale order named on the header, stamped on a create. */
    private void stampSaleOrder(Integer saleOrderId, Integer invoiceId, Integer companyId) {
        if (saleOrderId == null || saleOrderId == 0) {
            return;
        }
        jdbc.update("UPDATE SaleOrderMaster SET InvoiceNo = :invoiceId "
                        + "WHERE Id = :saleOrderId AND CompanyRefId = :companyId",
                new MapSqlParameterSource()
                        .addValue("invoiceId", invoiceId)
                        .addValue("saleOrderId", saleOrderId)
                        .addValue("companyId", companyId));
    }

    // ─────────────────────────────────────────────────────────── helpers ──

    private static List<String> bracketed(List<String> columns) {
        return columns.stream().map(column -> "[" + column + "]").toList();
    }

    private static List<String> named(List<String> columns) {
        return columns.stream().map(column -> ":" + column).toList();
    }

    /**
     * A reference id as the table wants it: NULL for "none".
     *
     * <p>This is the procedure's {@code if @X = ''} rule. Those look like
     * empty-string tests, but the variables are ints and T-SQL turns
     * {@code ''} into 0 — so a 0 coming from the screen is stored as NULL, and
     * the foreign keys stay clean instead of pointing at a row 0 that has
     * never existed.
     */
    static Integer refId(Integer value) {
        return value == null || value == 0 ? null : value;
    }

    /** Amount columns are NOT NULL in places; the procedure sends 0, so do we. */
    private static double money(Double value) {
        return value == null ? 0d : value;
    }

    private static int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    /** Text columns hold '' rather than NULL: the invoice list filters on both. */
    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static LocalDate date(LocalDate value) {
        return value;
    }

    private static LocalDateTime dateTime(LocalDateTime value) {
        return value;
    }

    /**
     * What the procedure wrote through {@code suser_name()} — the database
     * login. The application connects as one user, so this is that login, and
     * the acting employee is already recorded in LastEmployeeRefId.
     */
    private String auditUser() {
        String cached = auditUser;
        if (cached == null) {
            Map<String, Object> row = jdbc.queryForMap("SELECT SUSER_NAME() AS LoginName",
                    new MapSqlParameterSource());
            Object name = row.get("LoginName");
            cached = name == null ? "SYSTEM" : String.valueOf(name);
            auditUser = cached;
        }
        return cached;
    }
}
