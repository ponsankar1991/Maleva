package my.maleva.api.module.rti.dto;

import java.time.LocalDateTime;

public record RtiEmployeeAssignmentResponse(
        Integer id,
        Integer rtiMasterRefId,
        Integer saleOrderMasterRefId,
        LocalDateTime pickupDateD,
        LocalDateTime deliveryDateD,
        String originD,
        String destinationD,
        String rtiNumber,
        String remarks,
        Integer driverRefId,
        Integer truckRefId,
        Integer active,
        Integer pickupCount,
        Integer dropCount,
        String saleOrderNumber,
        String vesselNameRaw,
        String customerName,
        String commodity,
        String quantity,
        String truckSize,
        String employeeName,
        String driverName,
        String truckNumber,
        String truckType
) {}
