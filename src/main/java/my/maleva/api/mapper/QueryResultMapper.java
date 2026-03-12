package my.maleva.api.mapper;

import my.maleva.api.dto.SaleMasterViewModel;
import my.maleva.api.dto.SaleDetailsViewModel;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryResultMapper - MapStruct mapper for converting native query Object arrays to ViewModels
 * Provides type-safe and reusable mapping for complex query results
 */
@Mapper(componentModel = "spring")
@Component
public abstract class QueryResultMapper {

    /**
     * Map Object array from SaleMaster query to SaleMasterViewModel
     * 
     * Query returns 33 columns in this order:
     * 0=Id, 1=sportsaleorderid, 2=InvoiceId, 3=Remarks, 4=Destination, 5=FlighTime,
     * 6=Origin, 7=JobMasterRefId, 8=EmployeeName, 9=Offvesselname, 10=Sname,
     * 11=Loadingvesselname, 12=SPort, 13=OPort, 14=BillDate, 15=DETA (formatted date for sorting),
     * 16=ETA (raw datetime), 17=SETA (formatted), 18=SETB (formatted), 19=SOETA (formatted), 
     * 20=SOETB (formatted), 21=SPickupDate, 22=BillNoDisplay, 23=BillTime, 24=CustomerName, 
     * 25=JobType, 26=NetAmt, 27=SaleType, 28=BillNo, 29=JobStatus, 30=InvoiceNo, 31=QNECode, 32=QNEId
     * 
     * @param row the Object array from database query result
     * @return mapped SaleMasterViewModel or null if row is invalid
     */
    public SaleMasterViewModel mapSaleMasterRow(Object[] row) {
        if (row == null || row.length < 33) {
            return null;
        }

        SaleMasterViewModel vm = new SaleMasterViewModel();

        try {
            // Column 0 - Primary Key
            vm.setId(toInteger(row[0]));
            
            // Column 1-13 - Basic order information
            vm.setSportsaleorderid(toInteger(row[1]));
            vm.setInvoiceId(toInteger(row[2]));
            vm.setRemarks(toString(row[3]));
            vm.setDestination(toString(row[4]));
            vm.setFlighTime(toString(row[5]));
            vm.setOrigin(toString(row[6]));
            vm.setJobMasterRefId(toInteger(row[7]));
            vm.setEmployeeName(toString(row[8]));
            vm.setOffvesselname(toString(row[9]));
            vm.setSname(toString(row[10]));
            vm.setLoadingvesselname(toString(row[11]));
            vm.setSPort(toString(row[12]));
            vm.setOPort(toString(row[13]));
            
            // Column 14-15 - Date fields for sorting (already formatted from SQL)
            vm.setBillDate(toString(row[14]));
            vm.setDeta(toString(row[15]));  // Already formatted date for sorting
            
            // Column 16-20 - ETA/ETB datetime fields
            vm.setEta(toLocalDateTime(row[16]));  // Raw datetime object
            vm.setSeta(toString(row[17]));          // Formatted ETA
            vm.setSetb(toString(row[18]));          // Formatted ETB
            vm.setSoeta(toString(row[19]));         // Formatted OETA
            vm.setSoetb(toString(row[20]));         // Formatted OETB
            
            // Column 21-24 - Pickup and bill details
            vm.setSPickupDate(toString(row[21]));
            vm.setBillNoDisplay(toString(row[22]));
            vm.setBillTime(toString(row[23]));
            vm.setCustomerName(toString(row[24]));
            
            // Column 25-28 - Job type and amount information
            vm.setJobType(toString(row[25]));
            vm.setNetAmt(toDouble(row[26]));
            vm.setSaleType(toString(row[27]));
            vm.setBillNo(toInteger(row[28]));
            
            // Column 29-32 - Status and invoice information
            vm.setJobStatus(toString(row[29]));
            vm.setInvoiceNo(toString(row[30]));
            vm.setQneCode(toString(row[31]));
            vm.setQneId(toString(row[32]));

        } catch (Exception e) {
            return null;
        }

        return vm;
    }

    /**
     * Map list of Object arrays to SaleMasterViewModel list
     */
    public List<SaleMasterViewModel> mapSaleMasterRows(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        List<SaleMasterViewModel> result = new ArrayList<>();
        for (Object[] row : rows) {
            SaleMasterViewModel vm = mapSaleMasterRow(row);
            if (vm != null) {
                result.add(vm);
            }
        }
        return result;
    }

    /**
     * Map Object array from SaleDetails query to SaleDetailsViewModel
     * Query returns 14 columns in this order:
     * 0=DiscAmount, 1=DiscPer, 2=ItemQty, 3=MRP, 4=PName, 5=SDRemarks,
     * 6=SalesRate, 7=SaleOrderMasterRefId, 8=TaxAmount, 9=TaxPercent,
     * 10=Prod_Code, 11=Amount, 12=CurrencyValue, 13=ActualAmount
     */
    public SaleDetailsViewModel mapSaleDetailsRow(Object[] row) {
        if (row == null || row.length < 14) {
            return null;
        }

        SaleDetailsViewModel vm = new SaleDetailsViewModel();

        // Map all 14 fields with null-safe conversions
        vm.setDiscountAmt(toDouble(row[0]));
        vm.setDiscountPercent(toDouble(row[1]));
        vm.setItemQty(toDouble(row[2]));
        vm.setMrp(toDouble(row[3]));
        vm.setProductName(toString(row[4]));
        vm.setSdRemarks(toString(row[5]));
        vm.setSaleRate(toDouble(row[6]));
        vm.setSaleRefId(toInteger(row[7]));
        vm.setTaxAmt(toDouble(row[8]));
        vm.setTaxPercent(toDouble(row[9]));
        vm.setProductCode(toString(row[10]));
        vm.setSAmount(toDouble(row[11]));
        vm.setCurrencyValue(toDouble(row[12]));
        vm.setActualAmount(toDouble(row[13]));

        return vm;
    }

    /**
     * Map list of Object arrays to SaleDetailsViewModel list
     */
    public List<SaleDetailsViewModel> mapSaleDetailsRows(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        List<SaleDetailsViewModel> result = new ArrayList<>();
        for (Object[] row : rows) {
            SaleDetailsViewModel vm = mapSaleDetailsRow(row);
            if (vm != null) {
                result.add(vm);
            }
        }
        return result;
    }

    // ============ Helper Methods for Type Conversion ============

    /**
     * Safe conversion to LocalDateTime with null handling
     * Handles various datetime formats from SQL Server
     */
    protected LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.util.Date) {
            return new java.sql.Timestamp(((java.util.Date) value).getTime()).toLocalDateTime();
        }
        if (value instanceof String) {
            try {
                return LocalDateTime.parse((String) value);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Safe conversion to Integer with null handling
     */
    protected Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Safe conversion to Double with null handling
     */
    protected Double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Safe conversion to String with null handling
     * Returns empty string for null values (consistent with SQL ISNULL behavior)
     * This prevents null pointer exceptions in JSON serialization and maintains consistency
     * 
     * @param value the object to convert
     * @return empty string if value is null, trimmed string otherwise
     */
    protected String toString(Object value) {
        if (value == null) {
            return "";  // Consistent with SQL ISNULL('', '')
        }
        String result = value.toString().trim();
        return result.isEmpty() ? "" : result;
    }
}

