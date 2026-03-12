package my.maleva.api.util;

import my.maleva.api.dto.SaleDetailsViewModel;
import my.maleva.api.dto.SaleMasterViewModel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SaleOrderFormatter - Utility for formatting dates and data in responses
 * Moves date formatting from SQL layer to application layer (best practice)
 */
@Component
public class SaleOrderFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter VARCHAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Format LocalDateTime to dd/MM/yyyy
     */
    public String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "01/01/1900" : dateTime.format(DATE_FORMATTER);
    }

    /**
     * Format LocalDateTime to dd/MM/yyyy HH:mm:ss
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Format LocalDateTime to yyyy-MM-dd'T'HH:mm:ss.SSS (VARCHAR(26) style)
     */
    public String formatVarchar(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(VARCHAR_FORMATTER);
    }

    /**
     * Coalesce LocalDateTime - return first non-null value
     */
    public LocalDateTime coalesce(LocalDateTime... dateTimes) {
        for (LocalDateTime dt : dateTimes) {
            if (dt != null) {
                return dt;
            }
        }
        return null;
    }

    /**
     * Format LocalDateTime for DETA field (used for sorting)
     * Returns formatted date string, preferring one over another based on ETAType logic
     */
    public String formatDetaField(LocalDateTime eta, LocalDateTime oeta) {
        LocalDateTime selectedDate = coalesce(eta, oeta);
        return selectedDate == null ? "01/01/1900" : selectedDate.format(DATE_FORMATTER);
    }

    /**
     * Convert map-based row data to SaleMasterViewModel
     * This handles the complex projection from the original query
     */
    @SuppressWarnings("unchecked")
    public SaleMasterViewModel mapToSaleMasterViewModel(Map<String, Object> row) {
        return SaleMasterViewModel.builder()
                .id(getAsInteger(row.get("Id")))
                .sportsaleorderid(getAsInteger(row.get("sportsaleorderid")))
                .invoiceId(getAsInteger(row.get("InvoiceNo")))
                .remarks(getAsString(row.get("Remarks")))
                .destination(getAsString(row.get("Destination")))
                .flighTime(getAsString(row.get("FlighTime")))
                .origin(getAsString(row.get("Origin")))
                .jobMasterRefId(getAsInteger(row.get("JobMasterRefId")))
                .employeeName(getAsString(row.get("EmployeeName")))
                .offvesselname(getAsString(row.get("Offvesselname")))
                .sname(getAsString(row.get("Sname")))
                .loadingvesselname(getAsString(row.get("Loadingvesselname")))
                .sPort(getAsString(row.get("SPort")))
                .oPort(getAsString(row.get("OPort")))
                .billDate(getAsString(row.get("BillDate")))
                .deta(getAsString(row.get("DETA")))
                .eta(getAsLocalDateTime(row.get("ETA")))
                .seta(getAsString(row.get("SETA")))
                .setb(getAsString(row.get("SETB")))
                .soeta(getAsString(row.get("SOETA")))
                .soetb(getAsString(row.get("SOETB")))
                .sPickupDate(getAsString(row.get("SPickupDate")))
                .billNoDisplay(getAsString(row.get("BillNoDisplay")))
                .billTime(getAsString(row.get("BillTime")))
                .customerName(getAsString(row.get("CustomerName")))
                .jobType(getAsString(row.get("JobType")))
                .netAmt(getAsDouble(row.get("NetAmt")))
                .saleType(getAsString(row.get("SaleType")))
                .billNo(getAsInteger(row.get("BillNo")))
                .jobStatus(getAsString(row.get("JobStatus")))
                .invoiceNo(getAsString(row.get("InvoiceNo")))
                .qneCode(getAsString(row.get("QNECode")))
                .qneId(getAsString(row.get("QNEId")))
                .build();
    }

    /**
     * Convert map-based row data to SaleDetailsViewModel
     */
    @SuppressWarnings("unchecked")
    public SaleDetailsViewModel mapToSaleDetailsViewModel(Map<String, Object> row) {
        return SaleDetailsViewModel.builder()
                .discountAmt(getAsDouble(row.get("DiscAmount")))
                .discountPercent(getAsDouble(row.get("DiscPer")))
                .itemQty(getAsDouble(row.get("ItemQty")))
                .mrp(getAsDouble(row.get("MRP")))
                .productName(getAsString(row.get("PName")))
                .sdRemarks(getAsString(row.get("SDRemarks")))
                .saleRate(getAsDouble(row.get("SalesRate")))
                .saleRefId(getAsInteger(row.get("SaleOrderMasterRefId")))
                .taxAmt(getAsDouble(row.get("TaxAmount")))
                .taxPercent(getAsDouble(row.get("TaxPercent")))
                .productCode(getAsString(row.get("Prod_Code")))
                .sAmount(getAsDouble(row.get("Amount")))
                .currencyValue(getAsDouble(row.get("CurrencyValue")))
                .actualAmount(getAsDouble(row.get("ActualAmount")))
                .build();
    }

    // Helper methods for type conversion
    private Integer getAsInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Double getAsDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private String getAsString(Object value) {
        if (value == null) return "";
        return value.toString().trim();
    }

    private LocalDateTime getAsLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }
}

