package my.maleva.api.mapper;

import my.maleva.api.dto.PlanningDetailsModel;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * PlanningSearchResultMapper - MapStruct mapper for converting raw SQL results to DTOs
 * Handles mapping of Object[] arrays from native SQL queries to PlanningDetailsModel
 * This provides type-safe mapping with proper null handling
 */
@Mapper(componentModel = "spring")
public interface PlanningSearchResultMapper {

    /**
     * Map Object[] array from SQL query to PlanningDetailsModel
     * Handles all 45 fields from PLANINGSearch query result
     * 
     * @param row Object array from native SQL query
     * @return PlanningDetailsModel with all fields populated
     */
    @Named("mapSearchResult")
    default PlanningDetailsModel mapSearchResult(Object[] row) {
        if (row == null || row.length < 45) {
            return null;
        }

        return PlanningDetailsModel.builder()
                .employeeName(getString(row, 0))           // 0: EmployeeName
                .pickupDate(getLocalDateTime(row, 1))     // 1: PickupDate
                .pickupDateD(getString(row, 2))           // 2: PickupDateD
                .deliveryDateD(getString(row, 3))         // 3: DeliveryDateD
                .id(getInteger(row, 4))                   // 4: Id
                .jobNo(getString(row, 5))                 // 5: JobNo (CNumberDisplay)
                .sPickupDate(getString(row, 6))           // 6: SPickupDate
                .sDeliveryDate(getString(row, 7))         // 7: SDeliveryDate
                .wareHouseEnterDate(getLocalDateTime(row, 8))   // 8: WareHouseEnterDate
                .wareHouseExitDate(getLocalDateTime(row, 9))    // 9: WareHouseExitDate
                .sWareHouseEnterDate(getString(row, 10))  // 10: SWareHouseEnterDate
                .sWareHouseExitDate(getString(row, 11))   // 11: SWareHouseExitDate
                .wareHouseAddress(getString(row, 12))     // 12: WareHouseAddress
                .origin(getString(row, 13))               // 13: Origin
                .destination(getString(row, 14))          // 14: Destination
                .originD(getString(row, 15))              // 15: OriginD
                .destinationD(getString(row, 16))         // 16: DestinationD
                .pkg(getString(row, 17))                  // 17: pkg
                .vesselName(getString(row, 18))           // 18: VesselName
                .jobDate(getString(row, 19))              // 19: JobDate
                .customerName(getString(row, 20))         // 20: CustomerName
                .truckName(getString(row, 21))            // 21: TruckName (empty in search)
                .truckRefId(getInteger(row, 22))          // 22: TruckRefid (0 in search)
                .remarks(getString(row, 23))              // 23: Remarks (empty in search)
                .jobStatus(getString(row, 24))            // 24: JobStatus
                .pickupAddress(getString(row, 25))        // 25: PickupAddress
                .deliveryAddress(getString(row, 26))      // 26: DeliveryAddress
                .leta(getString(row, 27))                 // 27: LETA
                .oeta(getString(row, 28))                 // 28: OETA
                .jobName(getString(row, 29))              // 29: JobName
                .awbNo(getString(row, 30))                // 30: AWBNo
                .blCopy(getString(row, 31))               // 31: BLCopy
                .sPort(getString(row, 32))                // 32: SPort
                .oPort(getString(row, 33))                // 33: OPort
                .truckSize(getString(row, 34))            // 34: truckSize
                .pickupTimeList(getString(row, 35))       // 35: pickuptimelist
                .pickupQuantityList(getString(row, 36))   // 36: pickupQuantitylist
                .deliveryQuantityList(getString(row, 37)) // 37: DeliveryQuantitylist
                .deliveryTimeList(getString(row, 38))     // 38: Delivertimelist
                .sdId(getInteger(row, 39))                // 39: SDId (0)
                .planningMasterRefId(getInteger(row, 40)) // 40: PLANINGMasterRefId (0)
                .saleOrderMasterRefId(getInteger(row, 41))// 41: SaleOrderMasterRefId (0)
                .sortBy(getInteger(row, 42))              // 42: SortBy (0)
                .truckNameD(getString(row, 43))           // 43: TruckNameD (empty)
                .driverNameD(getString(row, 44))          // 44: DriverNameD (empty)
                .build();
    }

    /**
     * Map Object[] array from SelectPLANING detail query to PlanningDetailsModel
     * Handles all 23 fields from SelectPLANING query result
     */
    @Named("mapSelectPlanningResult")
    default PlanningDetailsModel mapSelectPlanningResult(Object[] row) {
        if (row == null || row.length < 23) {
            return null;
        }

        return PlanningDetailsModel.builder()
                .id(getInteger(row, 0))                    // 0: Id
                .sdId(getInteger(row, 1))                  // 1: SDId
                .planningMasterRefId(getInteger(row, 2))   // 2: PLANINGMasterRefId
                .saleOrderMasterRefId(getInteger(row, 3))  // 3: SaleOrderMasterRefId
                .truckRefId(getInteger(row, 4))            // 4: TruckRefid
                .truckName(getString(row, 5))              // 5: TruckName
                .driverName(getString(row, 6))             // 6: DriverName
                .jobNo(getString(row, 7))                  // 7: JobNo (CNumberDisplay)
                .jobDate(getString(row, 8))                // 8: JobDate
                .jobStatus(getString(row, 9))              // 9: JobStatus
                .originD(getString(row, 10))               // 10: OriginD
                .destinationD(getString(row, 11))          // 11: DestinationD
                .customerName(getString(row, 12))          // 12: CustomerName
                .remarks(getString(row, 13))               // 13: Remarks
                .truckNameD(getString(row, 14))            // 14: TruckNameD
                .driverNameD(getString(row, 15))           // 15: DriverNameD
                .sortBy(getInteger(row, 16))               // 16: SortBy
                .pickupDateD(getString(row, 17))           // 17: PickupDateD
                .deliveryDateD(getString(row, 18))         // 18: DeliveryDateD
                .pickupTimeList(getString(row, 19))        // 19: pickuptimelist
                .pickupQuantityList(getString(row, 20))    // 20: pickupQuantitylist
                .deliveryQuantityList(getString(row, 21))  // 21: DeliveryQuantitylist
                .deliveryTimeList(getString(row, 22))      // 22: Delivertimelist
                .build();
    }

    /**
     * Helper method to safely get String from Object array
     */
    default String getString(Object[] row, int index) {
        if (index < row.length && row[index] != null) {
            return (String) row[index];
        }
        return "";
    }

    /**
     * Helper method to safely get Integer from Object array
     */
    default Integer getInteger(Object[] row, int index) {
        if (index < row.length && row[index] != null) {
            Object value = row[index];
            if (value instanceof Integer integerValue) {
                return integerValue;
            }
            if (value instanceof Number numberValue) {
                return numberValue.intValue();
            }
        }
        return 0;
    }

    /**
     * Helper method to safely get LocalDateTime from Object array
     */
    default java.time.LocalDateTime getLocalDateTime(Object[] row, int index) {
        if (index < row.length && row[index] != null) {
            Object value = row[index];
            if (value instanceof java.time.LocalDateTime localDateTime) {
                return localDateTime;
            }
            if (value instanceof java.sql.Timestamp timestamp) {
                return timestamp.toLocalDateTime();
            }
        }
        return null;
    }
}

