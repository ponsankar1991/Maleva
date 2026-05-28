package my.maleva.api.module.common.mapper;

import my.maleva.api.module.common.dto.InventoryFilterDto;
import org.mapstruct.Mapper;

/**
 * InventoryFilterMapper - MapStruct mapper for InventoryFilterDto
 * Handles conversion for inventory filtering models
 */
@Mapper(componentModel = "spring")
public interface InventoryFilterMapper {

    /**
     * Convert InventoryFilterDto - identity mapping
     */
    InventoryFilterDto toDto(InventoryFilterDto filter);

    /**
     * Create InventoryFilterDto with basic parameters
     */
    default InventoryFilterDto createFilter(Integer comid, String fromDate, String toDate) {
        return InventoryFilterDto.builder()
                .comid(comid)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
    }

    /**
     * Create InventoryFilterDto with all parameters
     */
    default InventoryFilterDto createFullFilter(Integer comid, String fromDate, String toDate,
                                                Integer status, Integer customerId, Integer portType) {
        return InventoryFilterDto.builder()
                .comid(comid)
                .fromDate(fromDate)
                .toDate(toDate)
                .status(status)
                .customerId(customerId)
                .portType(portType)
                .build();
    }
}

