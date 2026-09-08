package my.maleva.api.module.saleorder.service;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceDetailRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceSaveResult;
import my.maleva.api.module.invoice.service.SaleInvoiceTransactionService;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.jobs.entity.JobStatusMaster;
import my.maleva.api.module.jobs.repository.JobStatusMasterRepository;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCreated;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceLink;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoicePreview;
import my.maleva.api.module.saleorder.dto.SaleOrderStatusUpdateDto;
import my.maleva.api.module.saleorder.entity.SaleOrderDetails;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.repository.SaleOrderDetailsRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.saleorder.util.SaleOrderApiConstants;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create Invoice, straight from the sale order screen.
 *
 * <p>Legacy had no such thing. Its Push Invoice button stashed the job id in
 * localStorage, opened the invoice screen in a new tab, and left the operator
 * to press Save there, then go back and change the job status by hand. This
 * does the three steps as one: preview what the invoice will be, save it
 * through the same {@code SP_SaleMaster} path the invoice screen uses, and
 * move the job to its completed status.
 *
 * <p>The mapping from sale order to invoice mirrors the invoice screen's own
 * push (SaleInvoice.tsx {@code loadMultiJobs}) field for field, and every
 * line is recalculated the way that screen does it: amount = qty x rate,
 * tax = amount x percent / 100, line total = amount + tax, each rounded to
 * two decimals. The header totals are the sums of those lines, which is how
 * the stored invoices read (header Amount = sum of line Amount, tax inclusive).
 *
 * <p>Two things are deliberately different from the screen:
 * <ul>
 *   <li>The invoice is dated today, not with the sale order's date. LHDN
 *       rejects an e-invoice submitted more than 72 hours after its invoice
 *       date, so a back-dated invoice created weeks after the job would be
 *       un-submittable.</li>
 *   <li>The status change is a separate step after the invoice is saved. If
 *       a status rule refuses it, the invoice is kept and the answer says the
 *       status was not changed and why, rather than throwing the invoice away.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SaleOrderInvoiceCreationService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderInvoiceCreationService.class);

    /** JobStatusMaster.Name of the completed status. Stored with a trailing space in the data. */
    static final String COMPLETED_STATUS_NAME = "JOB COMPLET";
    /**
     * BillType is the invoice series, not a payment term: every stored invoice
     * carries MY or TR, the same two letters that open its job number. When a
     * sale order has none, the job number says which series it belongs to.
     */
    private static final String DEFAULT_BILL_TYPE = "MY";

    private final SaleOrderMasterRepository saleOrders;
    private final SaleOrderDetailsRepository saleOrderDetails;
    private final ItemMasterRepository itemMasters;
    private final UomRepository uoms;
    private final JobStatusMasterRepository jobStatuses;
    private final SaleOrderInvoiceLinkService invoiceLinks;
    private final SaleInvoiceTransactionService invoices;
    private final SaleOrderMasterService saleOrderService;
    private final NamedParameterJdbcTemplate jdbc;

    // ─────────────────────────────────────────────────────────── preview ──

    @Transactional(readOnly = true)
    public SaleOrderInvoicePreview preview(Integer saleOrderId, Integer companyId) {
        SaleOrderMaster order = requireOrder(saleOrderId, companyId);
        List<SaleOrderDetails> details = saleOrderDetails.findBySaleOrderMasterRefId(order.getId());
        Catalog catalog = catalogFor(details);
        Names names = names(order);
        Optional<JobStatusMaster> completed = completedStatus(companyId);
        SaleOrderInvoiceLink link = invoiceLinks.find(saleOrderId, companyId).orElse(null);
        SaleOrderInvoiceLink existing = link != null && link.invoiced() ? link : null;

        SaleInvoiceRequestDTO request = toInvoiceRequest(order, details, catalog, completed.orElse(null));
        List<SaleOrderInvoicePreview.Line> lines = request.getDetails().stream()
                .map(d -> new SaleOrderInvoicePreview.Line(
                        d.getItemMasterRefId(), d.getProductCode(), d.getProductName(), d.getRemarks(),
                        d.getUom(), d.getItemQty(), d.getSalesRate(), d.getTaxPercent(),
                        d.getTaxAmount(), d.getAmount()))
                .toList();

        List<String> blockers = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (existing != null) {
            blockers.add("Invoice " + existing.invoiceNo() + " already bills this job");
        }
        if (isBlank(order.getCustomerRefId())) {
            blockers.add("The sale order has no customer");
        }
        if (isBlank(order.getJobMasterRefId())) {
            blockers.add("The sale order has no job type");
        }
        if (lines.isEmpty()) {
            blockers.add("The sale order has no item lines to invoice");
        }
        if (request.getDetails().stream().anyMatch(d -> isBlank(d.getItemMasterRefId()))) {
            blockers.add("A line has no product selected");
        }
        if (lines.stream().anyMatch(l -> l.amount() == null || l.amount() <= 0)) {
            warnings.add("At least one line has a zero amount");
        }
        if (completed.isEmpty()) {
            warnings.add("No \"" + COMPLETED_STATUS_NAME + "\" job status exists for this company, so the status will not change");
        }
        if (order.getCurrencyValue() == null || order.getCurrencyValue() == 0) {
            warnings.add("The sale order has no currency rate; 1.00 will be used");
        }

        return new SaleOrderInvoicePreview(
                order.getId(), orEmpty(order.getCNumberDisplay()),
                order.getCustomerRefId(), names.customer(),
                order.getJobMasterRefId(), names.jobType(),
                names.employee(),
                request.getSaleDate(), request.getBillType(),
                request.getCurrencyValue(), request.getSymbolRefId(),
                names.status(), completed.map(s -> s.getName().trim()).orElse(""),
                lines, sum(request.getDetails(), SaleOrderInvoiceCreationService::baseAmount),
                request.getTaxAmount(), request.getAmount(),
                existing, blockers.isEmpty(), blockers, warnings);
    }

    // ──────────────────────────────────────────────────────────── create ──

    /**
     * Saves the invoice, then moves the sale order to its completed status.
     * Not transactional on purpose: the invoice save owns its own transaction,
     * and a refused status change must not roll it back.
     */
    public SaleOrderInvoiceCreated create(Integer saleOrderId, Integer companyId) {
        SaleOrderInvoicePreview preview = preview(saleOrderId, companyId);
        if (!preview.canCreate()) {
            throw new InvalidRequestException(String.join("; ", preview.blockers()));
        }

        SaleOrderMaster order = requireOrder(saleOrderId, companyId);
        List<SaleOrderDetails> details = saleOrderDetails.findBySaleOrderMasterRefId(order.getId());
        Optional<JobStatusMaster> completed = completedStatus(companyId);
        SaleInvoiceRequestDTO request = toInvoiceRequest(order, details, catalogFor(details), completed.orElse(null));

        SaleInvoiceSaveResult saved = invoices.save(request);
        logger.info("Created invoice {} (id {}) from sale order {} for company {}",
                saved.getBillNo(), saved.getId(), saleOrderId, companyId);

        boolean statusUpdated = false;
        String statusMessage;
        Integer statusId = order.getJStatus();
        String statusName = preview.currentStatus();
        if (completed.isEmpty()) {
            statusMessage = "No \"" + COMPLETED_STATUS_NAME + "\" status exists for this company; the job status was left as it was";
        } else if (Objects.equals(order.getJStatus(), completed.get().getId())) {
            statusUpdated = true;
            statusId = completed.get().getId();
            statusName = completed.get().getName().trim();
            statusMessage = "The job was already " + statusName;
        } else {
            try {
                SaleOrderStatusUpdateDto updated =
                        saleOrderService.updateStatus(saleOrderId, companyId, completed.get().getId());
                statusUpdated = true;
                statusId = updated.getJStatus();
                statusName = orEmpty(updated.getStatusName()).trim();
                statusMessage = "Job status changed to " + statusName;
            } catch (RuntimeException ex) {
                // The invoice is saved and numbered; do not lose it over a
                // status rule. Say what happened instead.
                logger.warn("Invoice {} saved but sale order {} could not be completed: {}",
                        saved.getBillNo(), saleOrderId, ex.getMessage());
                statusMessage = "Invoice saved, but the job status was not changed: " + ex.getMessage();
            }
        }

        return new SaleOrderInvoiceCreated(
                saleOrderId, preview.jobNo(),
                saved.getId(), orEmpty(saved.getBillNo()),
                request.getSaleDate(), request.getAmount(),
                statusId, statusName, statusUpdated, statusMessage);
    }

    // ───────────────────────────────────────────────────────────── mapping ──

    /** The invoice the sale order becomes. Package-private for the tests. */
    SaleInvoiceRequestDTO toInvoiceRequest(SaleOrderMaster o, List<SaleOrderDetails> details,
                                           Catalog catalog, JobStatusMaster completed) {
        double currency = o.getCurrencyValue() == null || o.getCurrencyValue() == 0 ? 1.0 : o.getCurrencyValue();

        List<SaleInvoiceDetailRequestDTO> lines = new ArrayList<>();
        for (SaleOrderDetails d : details) {
            if (d == null) {
                continue;
            }
            ItemMaster item = d.getItemMasterRefId() == null ? null : catalog.items().get(d.getItemMasterRefId());
            String uom = item == null || item.getUomCode() == null ? "" : catalog.uomNames().getOrDefault(item.getUomCode(), "");
            double qty = nz(d.getItemQty());
            double rate = nz(d.getSalesRate());
            double taxPercent = nz(d.getTaxPercent());
            double base = qty * rate;
            double tax = round2(base * taxPercent / 100);
            double total = round2(base + tax);
            lines.add(SaleInvoiceDetailRequestDTO.builder()
                    .itemMasterRefId(d.getItemMasterRefId())
                    .taxRefId(d.getTaxRefId())
                    .itemQty(qty)
                    .salesRate(rate)
                    .discountPercent(nz(d.getDiscPer()))
                    .discountAmount(nz(d.getDiscAmount()))
                    .landingCost(0.0)
                    .taxPercent(taxPercent)
                    .taxAmount(tax)
                    .amount(total)
                    .productCode(item == null ? "" : orEmpty(item.getProdCode()))
                    .productName(item == null ? "" : orEmpty(item.getPName()))
                    .remarks(orEmpty(d.getSdRemarks()))
                    .uom(uom)
                    .currencyValue(currency)
                    .mrp(0.0)
                    .purchaseRate(0.0)
                    .netSalesRate(0.0)
                    .actualAmount(round2(total * currency))
                    .saleOrderMasterRefId(o.getId())
                    .build());
        }

        double taxTotal = sum(lines, SaleInvoiceDetailRequestDTO::getTaxAmount);
        double netTotal = sum(lines, SaleInvoiceDetailRequestDTO::getAmount);

        return SaleInvoiceRequestDTO.builder()
                .id(0)
                .companyRefId(o.getCompanyRefId())
                .userRefId(null)
                .employeeRefId(o.getEmployeeRefId())
                .customerRefId(o.getCustomerRefId())
                .jobMasterRefId(o.getJobMasterRefId())
                .agentCompanyRefId(o.getAgentCompanyRefId())
                .agentMasterRefId(o.getAgentMasterRefId())
                .oAgentCompanyRefId(o.getOAgentCompanyRefId())
                .oAgentMasterRefId(o.getOAgentMasterRefId())
                .saleDate(LocalDate.now())
                .billType(billTypeOf(o))
                .grossAmount(netTotal)
                .taxAmount(taxTotal)
                .discountAmount(0.0)
                .plusAmount(0.0)
                .minusAmount(0.0)
                .coinage(0.0)
                .amount(netTotal)
                .currencyValue(currency)
                .actualNetAmount(round2(netTotal * currency))
                .symbolRefId(o.getSymbolRefId())
                .remarks(orEmpty(o.getRemarks()))
                .remarks1(orEmpty(o.getRemarks1()))
                .doDescription(orEmpty(o.getDoDescription()))
                .offVesselName(orEmpty(o.getOffvesselname()))
                .loadingVesselName(orEmpty(o.getLoadingvesselname()))
                .truckSize(orEmpty(o.getTruckSize()))
                .sPort(orEmpty(o.getSPort()))
                .oPort(orEmpty(o.getOPort()))
                .scn(orEmpty(o.getScn()))
                .lscn(orEmpty(o.getLscn()))
                .vessel(orEmpty(o.getVessel()))
                .oVessel(orEmpty(o.getOVessel()))
                .commodity(orEmpty(o.getCommodity()))
                .cargo(orEmpty(o.getCargo()))
                .awbNo(orEmpty(o.getAwbNo()))
                .blCopy(orEmpty(o.getBlCopy()))
                .quantity(orEmpty(o.getQuantity()))
                .totalWeight(orEmpty(o.getTotalWeight()))
                .ptw(orEmpty(o.getPtw()))
                .origin(orEmpty(o.getOrigin()))
                .destination(orEmpty(o.getDestination()))
                .eta(o.getEta()).etb(o.getEtb()).etd(o.getEtd())
                .oEta(o.getOeta()).oEtb(o.getOetb()).oEtd(o.getOetd())
                .pickupDate(o.getPickupDate())
                .deliveryDate(o.getDeliveryDate())
                .wareHouseEnterDate(o.getWareHouseEnterDate())
                .wareHouseExitDate(o.getWareHouseExitDate())
                .pickupAddress(orEmpty(o.getPickupAddress()))
                .deliveryAddress(orEmpty(o.getDeliveryAddress()))
                .wareHouseAddress(orEmpty(o.getWareHouseAddress()))
                .docNo(o.getDocNo())
                .saleOrderMasterNo(o.getId())
                .truckRefId(o.getTruckRefid())
                .driverRefId(o.getDriverRefid())
                // The invoice carries the status the job is about to have.
                .jStatus(completed == null ? o.getJStatus() : completed.getId())
                .oStatus(0)
                .forkliftByRefId(o.getForkliftbyRefid())
                .sealByRefId(o.getSealbyRefid())
                .sealBreakByRefId(o.getSealbreakbyRefid())
                .sealByRefId2(o.getSealbyRefid2())
                .sealBreakByRefId2(o.getSealbreakbyRefid2())
                .sealByRefId3(o.getSealbyRefid3())
                .sealBreakByRefId3(o.getSealbreakbyRefid3())
                .boardingOfficerRefId(o.getBoardingOfficerRefid())
                .boardingOfficer1RefId(o.getBoardingOfficer1Refid())
                .boardingAmount(o.getBoardingAmount())
                .boardingAmount1(o.getBoardingAmount1())
                .forwarding(orEmpty(o.getForwarding()))
                .forwarding2(orEmpty(o.getForwarding2()))
                .forwarding3(orEmpty(o.getForwarding3()))
                .forwardingEnterRef(orEmpty(o.getForwardingEnterRef()))
                .forwardingExitRef(orEmpty(o.getForwardingExitRef()))
                .forwardingEnterRef2(orEmpty(o.getForwardingEnterRef2()))
                .forwardingExitRef2(orEmpty(o.getForwardingExitRef2()))
                .forwardingEnterRef3(orEmpty(o.getForwardingEnterRef3()))
                .forwardingExitRef3(orEmpty(o.getForwardingExitRef3()))
                .forwardingSmkNo(orEmpty(o.getForwardingSMKNo()))
                .forwardingSmkNo2(orEmpty(o.getForwardingSMKNo2()))
                .forwardingSmkNo3(orEmpty(o.getForwardingSMKNo3()))
                .portChargesRef(orEmpty(o.getPortChargesRef()))
                .portCharges(o.getPortCharges())
                .sealAmount(o.getSealAmount())
                .breakSealAmount(o.getBreakSealAmount())
                .sealAmount2(o.getSealAmount2())
                .breakSealAmount2(o.getBreakSealAmount2())
                .sealAmount3(o.getSealAmount3())
                .breakSealAmount3(o.getBreakSealAmount3())
                .zb(orEmpty(o.getZb()))
                .zb2(orEmpty(o.getZb2()))
                .zbRef(orEmpty(o.getZbRef()))
                .zbRef2(orEmpty(o.getZbRef2()))
                .details(lines)
                // SP_SaleMaster stamps SaleOrderMaster.InvoiceNo from this list.
                .saleOrderRefIds(List.of(o.getId()))
                .build();
    }

    // ───────────────────────────────────────────────────────────── lookups ──

    private SaleOrderMaster requireOrder(Integer saleOrderId, Integer companyId) {
        if (saleOrderId == null || saleOrderId <= 0 || companyId == null || companyId <= 0) {
            throw new InvalidRequestException("A sale order id and company are required");
        }
        SaleOrderMaster order = saleOrders.findByIdAndCompanyRefId(saleOrderId, companyId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sale order " + saleOrderId + " was not found for this company"));
        if (Objects.equals(order.getActive(), SaleOrderApiConstants.INACTIVE_STATUS)) {
            throw new InvalidRequestException("Sale order " + orEmpty(order.getCNumberDisplay()) + " is deleted");
        }
        return order;
    }

    /** The products on the lines and the names of their units. Package-private for the tests. */
    record Catalog(Map<Integer, ItemMaster> items, Map<Integer, String> uomNames) {
    }

    private Catalog catalogFor(List<SaleOrderDetails> details) {
        List<Integer> ids = details.stream()
                .filter(Objects::nonNull)
                .map(SaleOrderDetails::getItemMasterRefId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return new Catalog(Map.of(), Map.of());
        }
        Map<Integer, ItemMaster> items = itemMasters.findAllById(ids).stream()
                .collect(Collectors.toMap(ItemMaster::getId, Function.identity(), (a, b) -> a));
        // ItemMaster.UomCode is the Uom row id, not a code; the invoice line
        // wants the description the print shows (TRIP, UNIT, ...).
        List<Integer> uomIds = items.values().stream()
                .map(ItemMaster::getUomCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> uomNames = uomIds.isEmpty() ? Map.of() : uoms.findAllById(uomIds).stream()
                .collect(Collectors.toMap(Uom::getId, u -> orEmpty(u.getDescription()).trim(), (a, b) -> a));
        return new Catalog(items, uomNames);
    }

    /** The company's "JOB COMPLET" status, matched on the trimmed name. */
    Optional<JobStatusMaster> completedStatus(Integer companyId) {
        return jobStatuses.findSelectableByCompanyId(companyId, SaleOrderApiConstants.INACTIVE_STATUS).stream()
                .filter(s -> s.getName() != null && s.getName().trim().equalsIgnoreCase(COMPLETED_STATUS_NAME))
                .findFirst();
    }

    private record Names(String customer, String jobType, String employee, String status) {
    }

    private Names names(SaleOrderMaster o) {
        List<Names> rows = jdbc.query("""
                SELECT ISNULL(C.CustomerName, '') AS CustomerName,
                       ISNULL(JT.Name, '') AS JobTypeName,
                       ISNULL(E.EmployeeName, '') AS EmployeeName,
                       ISNULL(JS.Name, '') AS StatusName
                FROM SaleOrderMaster S WITH (NOLOCK)
                LEFT JOIN Customer C WITH (NOLOCK) ON C.Id = S.CustomerRefId
                LEFT JOIN JobTypeMaster JT WITH (NOLOCK) ON JT.Id = S.JobMasterRefId
                LEFT JOIN EmployeeMaster E WITH (NOLOCK) ON E.Id = S.EmployeeRefId
                LEFT JOIN JobStatusMaster JS WITH (NOLOCK) ON JS.Id = S.JStatus
                WHERE S.Id = :id
                """, new MapSqlParameterSource("id", o.getId()),
                (rs, i) -> new Names(rs.getString("CustomerName").trim(), rs.getString("JobTypeName").trim(),
                        rs.getString("EmployeeName").trim(), rs.getString("StatusName").trim()));
        return rows.isEmpty() ? new Names("", "", "", "") : rows.get(0);
    }

    // ───────────────────────────────────────────────────────────── helpers ──

    static String billTypeOf(SaleOrderMaster o) {
        if (!isBlankText(o.getBillType())) {
            return o.getBillType().trim();
        }
        String jobNo = orEmpty(o.getCNumberDisplay()).trim();
        if (jobNo.length() >= 2 && Character.isLetter(jobNo.charAt(0)) && Character.isLetter(jobNo.charAt(1))) {
            return jobNo.substring(0, 2).toUpperCase();
        }
        return DEFAULT_BILL_TYPE;
    }

    private static double baseAmount(SaleInvoiceDetailRequestDTO d) {
        return round2(nz(d.getItemQty()) * nz(d.getSalesRate()));
    }

    private static <T> double sum(List<T> rows, Function<T, Double> value) {
        double total = 0;
        for (T row : rows) {
            total += nz(value.apply(row));
        }
        return round2(total);
    }

    static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static double nz(Double value) {
        return value == null ? 0 : value;
    }

    private static boolean isBlank(Integer id) {
        return id == null || id <= 0;
    }

    private static boolean isBlankText(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String orEmpty(String s) {
        return s == null ? "" : s;
    }
}
