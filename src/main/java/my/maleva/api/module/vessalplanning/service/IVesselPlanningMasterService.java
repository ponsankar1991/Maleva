package my.maleva.api.module.vessalplanning.service;

import my.maleva.api.module.vessalplanning.dto.VesselPlanningLegacyDtos;

/**
 * IVesselPlanningMasterService - Interface for Vessel Planning Master Service
 * Handles legacy operations for vessel planning selection, edit, search, and view
 */
public interface IVesselPlanningMasterService {

    /**
     * Select vessel planning records with filtering
     */
    VesselPlanningLegacyDtos.F5View selectVesselPlanning(VesselPlanningLegacyDtos.F5Request filter);

    /**
     * Edit vessel planning record
     */
    VesselPlanningLegacyDtos.EditResponse editVesselPlanning(Integer id, Integer vesselPlanningNo, Integer companyId);

    /**
     * Search vessel planning records
     */
    java.util.List<VesselPlanningLegacyDtos.DetailsModel> vesselPlanningSearch(VesselPlanningLegacyDtos.SearchRequest filter);

    /**
     * View vessel planning records
     */
    java.util.List<VesselPlanningLegacyDtos.ViewModel> vesselPlanningView(Integer soId, Integer companyId);
}

