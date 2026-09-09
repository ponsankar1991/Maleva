package my.maleva.api.module.invoice.service;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceEditDto;
import my.maleva.api.module.invoice.entity.SaleDetails;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleDetailsRepository;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.TaxMaster;
import my.maleva.api.module.master.repository.TaxMasterRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loads one saved invoice back into the entry screen.
 *
 * <p>This replaces the call the screen used to make to the old .NET server,
 * {@code POST /api/SaleInvoiceApp/EditSaleInvoice}. That action does not
 * exist on that controller — the server answers "No HTTP resource was found"
 * — so opening an invoice for editing failed outright. The React side also
 * only ever built the header from the answer and returned an empty line
 * list, so even a working endpoint would have opened an invoice with no
 * lines. Both halves are fixed here: the whole invoice comes back in one
 * call, already shaped like the form.
 *
 * <p>Read-only. {@link SaleInvoiceTransactionService} is the only writer.
 */
@Service
@RequiredArgsConstructor
public class SaleInvoiceEditService {

    /** What a date input reads. */
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** What a datetime-local input reads; anything longer is rejected by the control. */
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    /** The separator the screen writes between multiple addresses in one column. */
    private static final String ADDRESS_SEPARATOR = "{@}";
    private static final int ACTIVE = 1;

    private final SaleMasterRepository saleMasters;
    private final SaleDetailsRepository saleDetails;
    private final ItemMasterRepository itemMasters;
    private final TaxMasterRepository taxes;
    private final NamedParameterJdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public SaleInvoiceEditDto load(Integer invoiceId, Integer companyId) {
        if (invoiceId == null || invoiceId <= 0 || companyId == null || companyId <= 0) {
            throw new InvalidRequestException("An invoice id and company are required");
        }
        SaleMaster invoice = saleMasters.findById(invoiceId)
                .filter(row -> Objects.equals(row.getCompanyRefId(), companyId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Invoice " + invoiceId + " was not found for this company"));
        if (Objects.equals(invoice.getActive(), 0)) {
            throw new InvalidRequestException(
                    "Invoice " + orEmpty(invoice.getCNumberDisplay()) + " has been deleted");
        }

        return SaleInvoiceEditDto.builder()
                .form(toForm(invoice))
                .lines(toLines(invoice))
                .pickupList(splitAddresses(invoice.getPickupAddress()))
                .deliveryList(splitAddresses(invoice.getDeliveryAddress()))
                .currencyValue(invoice.getCurrencyValue() == null ? 0d : invoice.getCurrencyValue())
                .symbolId(invoice.getSymbolRefId() == null ? 0 : invoice.getSymbolRefId())
                .build();
    }

    // ──────────────────────────────────────────────────────────── header ──

    private SaleInvoiceEditDto.Form toForm(SaleMaster i) {
        return SaleInvoiceEditDto.Form.builder()
                .id(i.getId())
                .invoiceNo(orEmpty(i.getCNumberDisplay()))
                .invoiceDate(date(i.getSaleDate()))
                .customerId(id(i.getCustomerRefId()))
                .jobTypeId(id(i.getJobMasterRefId()))
                .statusId(id(i.getJStatus()))
                .saleType(orEmpty(i.getSaleType()))
                .billType(orEmpty(i.getBillType()))
                .description(orEmpty(i.getDoDescription()))
                .remarks(orEmpty(i.getRemarks()))
                .remarks1(orEmpty(i.getRemarks1()))
                .loadingVesselName(orEmpty(i.getLoadingvesselname()))
                .offVesselName(orEmpty(i.getOffvesselname()))
                .awbNo(orEmpty(i.getAwbNo()))
                .ptwNo(orEmpty(i.getPtw()))
                .blCopy(orEmpty(i.getBlCopy()))
                .quantity(orEmpty(i.getQuantity()))
                .weight(orEmpty(i.getTotalWeight()))
                .truckSize(orEmpty(i.getTruckSize()))
                .loadingPort(orEmpty(i.getSPort()))
                .offPort(orEmpty(i.getOPort()))
                .loadingVesselType(orEmpty(i.getVessel()))
                .offVesselType(orEmpty(i.getOVessel()))
                .commodity(orEmpty(i.getCommodity()))
                .cargo(orEmpty(i.getCargo()))
                .loadingScn(orEmpty(i.getLscn()))
                .offScn(orEmpty(i.getScn()))
                .pickupAddress(orEmpty(i.getPickupAddress()))
                .deliveryAddress(orEmpty(i.getDeliveryAddress()))
                .warehouseAddress(orEmpty(i.getWareHouseAddress()))
                .origin(orEmpty(i.getOrigin()))
                .destination(orEmpty(i.getDestination()))
                .forkliftId(id(i.getForkliftbyRefid()))
                .agentCompanyId(id(i.getAgentCompanyRefId()))
                .agentId(id(i.getAgentMasterRefId()))
                .offAgentCompanyId(id(i.getOAgentCompanyRefId()))
                .offAgentId(id(i.getOAgentMasterRefId()))
                .forwarding1(orEmpty(i.getForwarding()))
                .forwarding2(orEmpty(i.getForwarding2()))
                .forwarding3(orEmpty(i.getForwarding3()))
                .forwardingEnterRef1(orEmpty(i.getForwardingEnterRef()))
                .forwardingExitRef1(orEmpty(i.getForwardingExitRef()))
                .forwardingEnterRef2(orEmpty(i.getForwardingEnterRef2()))
                .forwardingExitRef2(orEmpty(i.getForwardingExitRef2()))
                .forwardingEnterRef3(orEmpty(i.getForwardingEnterRef3()))
                .forwardingExitRef3(orEmpty(i.getForwardingExitRef3()))
                .forwardingSmkNo1(orEmpty(i.getForwardingSMKNo()))
                .forwardingSmkNo2(orEmpty(i.getForwardingSMKNo2()))
                .forwardingSmkNo3(orEmpty(i.getForwardingSMKNo3()))
                .sealBy1(id(i.getSealbyRefid()))
                .breakSealBy1(id(i.getSealbreakbyRefid()))
                .sealBy2(id(i.getSealbyRefid2()))
                .breakSealBy2(id(i.getSealbreakbyRefid2()))
                .sealBy3(id(i.getSealbyRefid3()))
                .breakSealBy3(id(i.getSealbreakbyRefid3()))
                .sealAmount1(amount(i.getSealAmount()))
                .breakSealAmount1(amount(i.getBreakSealAmount()))
                .sealAmount2(amount(i.getSealAmount2()))
                .breakSealAmount2(amount(i.getBreakSealAmount2()))
                .sealAmount3(amount(i.getSealAmount3()))
                .breakSealAmount3(amount(i.getBreakSealAmount3()))
                .zb1(orEmpty(i.getZb()))
                .zb2(orEmpty(i.getZb2()))
                .zbRef1(orEmpty(i.getZbRef()))
                .zbRef2(orEmpty(i.getZbRef2()))
                .boardingOfficer1(id(i.getBoardingOfficerRefid()))
                .boardingOfficer2(id(i.getBoardingOfficer1Refid()))
                .boardingAmount1(amount(i.getBoardingAmount()))
                .boardingAmount2(amount(i.getBoardingAmount1()))
                .portChargesRef(orEmpty(i.getPortChargesRef()))
                .portCharges(amount(i.getPortCharges()))
                .eta(dateTime(i.getEta()))
                .etb(dateTime(i.getEtb()))
                .etd(dateTime(i.getEtd()))
                .offEta(dateTime(i.getOeta()))
                .offEtb(dateTime(i.getOetb()))
                .offEtd(dateTime(i.getOetd()))
                .pickupDate(dateTime(i.getPickupDate()))
                .deliveryDate(dateTime(i.getDeliveryDate()))
                .warehouseEnterDate(dateTime(i.getWareHouseEnterDate()))
                .warehouseExitDate(dateTime(i.getWareHouseExitDate()))
                .build();
    }

    // ───────────────────────────────────────────────────────────── lines ──

    private List<SaleInvoiceEditDto.Line> toLines(SaleMaster invoice) {
        List<SaleDetails> details = saleDetails.findBySaleMasterRefId(invoice.getId()).stream()
                .sorted(Comparator.comparing(SaleDetails::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        if (details.isEmpty()) {
            return List.of();
        }

        Map<Integer, ItemMaster> items = itemMasters.findAllById(
                        details.stream().map(SaleDetails::getItemMasterRefId)
                                .filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ItemMaster::getId, Function.identity(), (a, b) -> a));
        Map<Integer, String> taxCodes = taxes.findAllById(
                        details.stream().map(SaleDetails::getTaxRefId)
                                .filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(TaxMaster::getId, t -> orEmpty(t.getCode()), (a, b) -> a));
        Map<Integer, String> jobNumbers = jobNumbers(details);

        List<SaleInvoiceEditDto.Line> lines = new ArrayList<>(details.size());
        for (SaleDetails d : details) {
            ItemMaster item = d.getItemMasterRefId() == null ? null : items.get(d.getItemMasterRefId());
            lines.add(SaleInvoiceEditDto.Line.builder()
                    .id(d.getId())
                    .productCode(item == null ? "" : orEmpty(item.getProdCode()))
                    .productName(item == null ? "" : orEmpty(item.getPName()))
                    .sdRemarks(orEmpty(d.getSdRemarks()))
                    .itemMasterRefId(d.getItemMasterRefId())
                    .itemQty(nz(d.getItemQty()))
                    .salesRate(nz(d.getSalesRate()))
                    .taxCode(d.getTaxRefId() == null ? "" : taxCodes.getOrDefault(d.getTaxRefId(), ""))
                    .taxPercent(nz(d.getTaxPercent()))
                    .taxRefId(d.getTaxRefId())
                    .taxAmount(nz(d.getTaxAmount()))
                    .amount(nz(d.getAmount()))
                    .currencyValue(nz(d.getCurrencyValue()))
                    .actualAmount(nz(d.getActualAmount()))
                    .saleOrderMasterRefId(d.getSaleOrderMasterRefId())
                    .saleMasterRefId(d.getSaleMasterRefId())
                    .saleJobNo(d.getSaleOrderMasterRefId() == null
                            ? "" : jobNumbers.getOrDefault(d.getSaleOrderMasterRefId(), ""))
                    .build());
        }
        return lines;
    }

    /** Job number per sale order id, for the grid's Job No column. */
    private Map<Integer, String> jobNumbers(List<SaleDetails> details) {
        List<Integer> ids = details.stream()
                .map(SaleDetails::getSaleOrderMasterRefId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Map.Entry<Integer, String>> rows = jdbc.query(
                "SELECT Id, ISNULL(CNumberDisplay, '') AS JobNo FROM SaleOrderMaster WITH (NOLOCK) "
                        + "WHERE Id IN (:ids)",
                new MapSqlParameterSource("ids", ids),
                (rs, i) -> Map.entry(rs.getInt("Id"), rs.getString("JobNo")));
        return rows.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
    }

    // ─────────────────────────────────────────────────────────── helpers ──

    /**
     * One column holds every pickup or delivery address, joined by "{@}".
     * A column with no separator is a single address, and an empty one is no
     * address at all — not one empty row, which would show as a blank line
     * the operator has to delete before saving.
     */
    static List<String> splitAddresses(String value) {
        String text = orEmpty(value).trim();
        if (text.isEmpty()) {
            return List.of();
        }
        List<String> addresses = new ArrayList<>();
        for (String part : text.split(java.util.regex.Pattern.quote(ADDRESS_SEPARATOR))) {
            String address = part.trim();
            if (!address.isEmpty()) {
                addresses.add(address);
            }
        }
        return addresses;
    }

    /** A reference id as the form holds it: "" for nothing, never "0" or "null". */
    static String id(Integer value) {
        return value == null || value == 0 ? "" : String.valueOf(value);
    }

    /** An amount as the form holds it: "" for nothing, so the box shows empty. */
    static String amount(Double value) {
        return value == null || value == 0d ? "" : trimZeros(value);
    }

    private static String trimZeros(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    static String date(LocalDateTime value) {
        return value == null ? "" : value.format(DATE);
    }

    static String dateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    private static double nz(Double value) {
        return value == null ? 0 : value;
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
