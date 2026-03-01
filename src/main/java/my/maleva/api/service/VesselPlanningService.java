package my.maleva.api.service;

import my.maleva.api.dto.VesselPlanningMasterDto;
import my.maleva.api.dto.VesselPlanningDetailsDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * VesselPlanningService - Business logic for VesselPlanning
 * Incorporates SP_VESSELPLANINGMaster stored procedure logic
 */
public interface VesselPlanningService {

    List<VesselPlanningMasterDto> getByCompanyRefId(Integer companyRefId);

    List<VesselPlanningMasterDto> getActiveByCompanyRefId(Integer companyRefId);

    Optional<VesselPlanningMasterDto> getByCNumber(Integer cNumber, Integer companyRefId);

    List<VesselPlanningMasterDto> getByUserRefId(Integer userRefId);

    List<VesselPlanningMasterDto> getByEmployeeRefId(Integer employeeRefId);

    List<VesselPlanningMasterDto> getByDateRange(LocalDate startDate, LocalDate endDate);

    List<VesselPlanningMasterDto> getByCompanyAndDateRange(Integer companyRefId, LocalDate startDate, LocalDate endDate);

    Optional<VesselPlanningMasterDto> getById(Integer id);

    VesselPlanningMasterDto create(VesselPlanningMasterDto dto);

    VesselPlanningMasterDto update(Integer id, VesselPlanningMasterDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countActiveByCompanyRefId(Integer companyRefId);

    void validateVesselPlanningData(VesselPlanningMasterDto dto);

    VesselPlanningMasterDto activateVesselPlanning(Integer id);

    VesselPlanningMasterDto deactivateVesselPlanning(Integer id);

    VesselPlanningMasterDto processVesselPlanning(VesselPlanningMasterDto dto, List<VesselPlanningDetailsDto> details, Integer companyId);
}

