package my.maleva.api.module.vessalplanning.service;

import my.maleva.api.module.vessalplanning.dto.VesselPlanningLegacyDtos;
import java.util.List;

/**
 * IVesselPlanningSaveService - Interface for Vessel Planning Save Service
 * Handles saving and deleting vessel planning records
 */
public interface IVesselPlanningSaveService {

    /**
     * Get the next sequence number for vessel planning
     */
    String getMaxVesselPlanningNo(Integer companyId);

    /**
     * Save multiple vessel planning records
     */
    List<VesselPlanningLegacyDtos.SaveResponse> saveAll(
            List<VesselPlanningLegacyDtos.SaveRequest> requests,
            Integer companyId);

    /**
     * Delete a vessel planning record
     */
    VesselPlanningLegacyDtos.SaveResponse delete(Integer id, Integer companyId);
}

