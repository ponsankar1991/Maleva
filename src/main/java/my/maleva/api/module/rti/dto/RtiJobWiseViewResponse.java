package my.maleva.api.module.rti.dto;

import java.time.LocalDate;

public record RtiJobWiseViewResponse(
        Long jobRefId,
        String rtiNumber,
        String jobNumber,
        String vesselName,
        String cargoDetails,
        String collectAt,
        String deliveryAt,
        LocalDate collectionDate,
        LocalDate deliveryDate,
        String truckNumber,
        String driverName,
        String truckSize,
        Integer pickupCount,
        Integer dropCount,
        String remarks,
        Integer legCount
) {}
