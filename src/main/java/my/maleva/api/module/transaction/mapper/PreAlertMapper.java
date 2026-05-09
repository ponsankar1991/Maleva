package my.maleva.api.module.transaction.mapper;

import my.maleva.api.module.transaction.dto.PreAlertDto;
import my.maleva.api.module.transaction.dto.PreAlertMasterDto;
import my.maleva.api.module.transaction.entity.PreAlert;
import my.maleva.api.module.transaction.entity.PreAlertMaster;
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

    /**
     * Convert PreAlertDto to Map for stored procedure input.
     * Used for SP_PreAlert insertion with proper field naming.
     */
    default java.util.Map<String, Object> toLegacyMap(PreAlertDto dto) {
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("Id", dto.getId() != null ? dto.getId() : 0);
        map.put("CustomerMasterRefId", dto.getCustomerMasterRefId());
        map.put("EmployeeMasterRefId", dto.getEmployeeMasterRefId());
        map.put("JobTypeMasterRefId", dto.getJobTypeMasterRefId());
        map.put("SaleOrderMasterRefId", dto.getSaleOrderMasterRefId());
        map.put("JobStatusMasterRefId", dto.getJobStatusMasterRefId());
        map.put("BoardingOfficerRefId", dto.getBoardingOfficerRefId());
        map.put("BoardingOfficerName", dto.getBoardingOfficerName());
        map.put("ShipName", dto.getShipName());
        map.put("Vessel", dto.getVessel());
        map.put("Commodity", dto.getCommodity());
        map.put("ETA", dto.getEta());
        map.put("ETB", dto.getEtb());
        map.put("ETD", dto.getEtd());
        map.put("JobNo", dto.getJobNo());
        map.put("Port", dto.getPort());
        map.put("Weight", dto.getWeight());
        map.put("Package", dto.getPackageInfo());
        map.put("AWBNo", dto.getAwbNo());
        map.put("AgentName", dto.getAgentName());
        map.put("AgentPhone", dto.getAgentPhone());
        map.put("Remarks", dto.getRemarks());
        map.put("SCN", dto.getScn());
        map.put("Active", dto.getActive() != null ? dto.getActive() : 1);
        map.put("PreAlertMasterRefId", dto.getPreAlertMasterRefId());
        return map;
    }

    /**
     * Convert PreAlert DTO to entity
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
    PreAlert toEntity(PreAlertDto dto);

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
    @Mapping(source = "CNumber", target = "cNumber")
    @Mapping(source = "CNumberDisplay", target = "cNumberDisplay")
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
     * Convert PreAlertMasterDto to Map for stored procedure input.
     * Used for SP_PreAlert master record insertion with proper field naming.
     */
    default java.util.Map<String, Object> masterToLegacyMap(PreAlertMasterDto dto) {
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("Id", dto.getId() != null ? dto.getId() : 0);
        map.put("CompanyRefId", dto.getCompanyRefId() != null ? dto.getCompanyRefId() : 0);
        map.put("CustomerMasterRefId", dto.getCustomerMasterRefId() != null ? dto.getCustomerMasterRefId() : 0);
        map.put("JobTypeMasterRefId", dto.getJobTypeMasterRefId() != null ? dto.getJobTypeMasterRefId() : 0);
        map.put("EmployeeRefId", dto.getEmployeeRefId() != null ? dto.getEmployeeRefId() : 0);
        map.put("CNumber", dto.getCNumber() != null ? dto.getCNumber() : 0);
        map.put("Port", dto.getPort() != null ? dto.getPort() : "");
        map.put("BoardingOfficerName", dto.getBoardingOfficerName() != null ? dto.getBoardingOfficerName() : "");
        map.put("Date", dto.getDate());
        map.put("Vessel", dto.getVessel() != null ? dto.getVessel() : "");
        map.put("OETA", dto.getOeta() != null ? dto.getOeta() : "");
        map.put("LETA", dto.getLeta() != null ? dto.getLeta() : "");
        map.put("ALLETA", dto.getAlleta() != null ? dto.getAlleta() : "");
        map.put("NONE", dto.getNone() != null ? dto.getNone() : "");
        map.put("ChkPort", dto.getChkPort() != null ? dto.getChkPort() : "");
        map.put("ChkVessel", dto.getChkVessel() != null ? dto.getChkVessel() : "");
        map.put("ChkPickupDate", dto.getChkPickupDate() != null ? dto.getChkPickupDate() : "");
        map.put("CNumberDisplay", dto.getCNumberDisplay() != null ? dto.getCNumberDisplay() : "");
        map.put("ChkConsolidated", dto.getChkConsolidated() != null ? dto.getChkConsolidated() : "");
        map.put("ChkDeliveryDone", dto.getChkDeliveryDone() != null ? dto.getChkDeliveryDone() : "");
        map.put("FromDate", dto.getFromDate());
        map.put("EntryDate", dto.getEntryDate());
        map.put("ToDate", dto.getToDate());
        map.put("Active", dto.getActive() != null ? dto.getActive() : 1);
        // PreAlert field will be set separately as JSON string
        return map;
    }
}
