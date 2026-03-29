package my.maleva.api.module.prealert.mapper;

import my.maleva.api.module.prealert.dto.PreAlertMasterDto;
import my.maleva.api.module.prealert.entity.PreAlertMaster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PreAlertMasterMapper {

    /**
     * Convert PreAlertMaster entity to DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "customerMasterRefId", target = "customerMasterRefId")
    @Mapping(source = "jobTypeMasterRefId", target = "jobTypeMasterRefId")
    @Mapping(source = "fromDate", target = "fromDate")
    @Mapping(source = "toDate", target = "toDate")
    @Mapping(source = "port", target = "port")
    @Mapping(source = "vessel", target = "vessel")
    @Mapping(source = "oeta", target = "oeta")
    @Mapping(source = "leta", target = "leta")
    @Mapping(source = "alleta", target = "alleta")
    @Mapping(source = "none", target = "none")
    @Mapping(source = "chkPort", target = "chkPort")
    @Mapping(source = "chkVessel", target = "chkVessel")
    @Mapping(source = "chkPickupDate", target = "chkPickupDate")
    @Mapping(source = "chkConsolidated", target = "chkConsolidated")
    @Mapping(source = "chkDeliveryDone", target = "chkDeliveryDone")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "CNumber", target = "CNumber")
    @Mapping(source = "CNumberDisplay", target = "CNumberDisplay")
    @Mapping(source = "entryDate", target = "entryDate")
    @Mapping(source = "saleOrderMasterRefId", target = "saleOrderMasterRefId")
    PreAlertMasterDto toDto(PreAlertMaster entity);

    /**
     * Convert PreAlertMaster DTO to entity
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "customerMasterRefId", target = "customerMasterRefId")
    @Mapping(source = "jobTypeMasterRefId", target = "jobTypeMasterRefId")
    @Mapping(source = "fromDate", target = "fromDate")
    @Mapping(source = "toDate", target = "toDate")
    @Mapping(source = "port", target = "port")
    @Mapping(source = "vessel", target = "vessel")
    @Mapping(source = "oeta", target = "oeta")
    @Mapping(source = "leta", target = "leta")
    @Mapping(source = "alleta", target = "alleta")
    @Mapping(source = "none", target = "none")
    @Mapping(source = "chkPort", target = "chkPort")
    @Mapping(source = "chkVessel", target = "chkVessel")
    @Mapping(source = "chkPickupDate", target = "chkPickupDate")
    @Mapping(source = "chkConsolidated", target = "chkConsolidated")
    @Mapping(source = "chkDeliveryDone", target = "chkDeliveryDone")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "CNumber", target = "CNumber")
    @Mapping(source = "CNumberDisplay", target = "CNumberDisplay")
    @Mapping(source = "entryDate", target = "entryDate")
    @Mapping(source = "saleOrderMasterRefId", target = "saleOrderMasterRefId")
    PreAlertMaster toEntity(PreAlertMasterDto dto);

    /**
     * Update entity from DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "customerMasterRefId", target = "customerMasterRefId")
    @Mapping(source = "jobTypeMasterRefId", target = "jobTypeMasterRefId")
    @Mapping(source = "fromDate", target = "fromDate")
    @Mapping(source = "toDate", target = "toDate")
    @Mapping(source = "port", target = "port")
    @Mapping(source = "vessel", target = "vessel")
    @Mapping(source = "oeta", target = "oeta")
    @Mapping(source = "leta", target = "leta")
    @Mapping(source = "alleta", target = "alleta")
    @Mapping(source = "none", target = "none")
    @Mapping(source = "chkPort", target = "chkPort")
    @Mapping(source = "chkVessel", target = "chkVessel")
    @Mapping(source = "chkPickupDate", target = "chkPickupDate")
    @Mapping(source = "chkConsolidated", target = "chkConsolidated")
    @Mapping(source = "chkDeliveryDone", target = "chkDeliveryDone")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "CNumber", target = "CNumber")
    @Mapping(source = "CNumberDisplay", target = "CNumberDisplay")
    @Mapping(source = "entryDate", target = "entryDate")
    @Mapping(source = "saleOrderMasterRefId", target = "saleOrderMasterRefId")
    void updateEntityFromDto(PreAlertMasterDto dto, @MappingTarget PreAlertMaster entity);
}




