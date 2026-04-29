package my.maleva.api.module.prealert.mapper;

import my.maleva.api.module.prealert.dto.PreAlertDto;
import my.maleva.api.module.prealert.entity.PreAlert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PreAlertMapper {

    /**
     * Convert PreAlert entity to DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "customerMasterRefId", target = "customerMasterRefId")
    @Mapping(source = "employeeMasterRefId", target = "employeeMasterRefId")
    @Mapping(source = "jobTypeMasterRefId", target = "jobTypeMasterRefId")
    @Mapping(source = "jobStatusMasterRefId", target = "jobStatusMasterRefId")
    @Mapping(source = "shipName", target = "shipName")
    @Mapping(source = "vessel", target = "vessel")
    @Mapping(source = "commodity", target = "commodity")
    @Mapping(source = "eta", target = "eta")
    @Mapping(source = "etb", target = "etb")
    @Mapping(source = "etd", target = "etd")
    @Mapping(source = "jobNo", target = "jobNo")
    @Mapping(source = "port", target = "port")
    @Mapping(source = "weight", target = "weight")
    @Mapping(source = "packageInfo", target = "packageInfo")
    @Mapping(source = "awbNo", target = "awbNo")
    @Mapping(source = "agentName", target = "agentName")
    @Mapping(source = "agentPhone", target = "agentPhone")
    @Mapping(source = "remarks", target = "remarks")
    @Mapping(source = "scn", target = "scn")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "preAlertMasterRefId", target = "preAlertMasterRefId")
    @Mapping(source = "boardingOfficerRefId", target = "boardingOfficerRefId")
    @Mapping(source = "boardingOfficerName", target = "boardingOfficerName")
    @Mapping(source = "saleOrderMasterRefId", target = "saleOrderMasterRefId")
    PreAlertDto toDto(PreAlert entity);
}
