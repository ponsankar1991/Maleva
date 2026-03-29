package my.maleva.api.module.planning.mapper;

import my.maleva.api.module.planning.dto.PlanningDetailsModel;
import my.maleva.api.module.planning.dto.PlanningF5View;
import my.maleva.api.module.planning.dto.PlanningMasterViewModel;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

/**
 * PlanningF5ViewMapper - MapStruct mapper for PlanningF5View
 * Handles mapping between ViewModels for SelectPLANING response
 */
@Mapper(componentModel = "spring")
public interface PlanningF5ViewMapper {

    /**
     * Create PlanningF5View from separate lists of master and detail records
     * @param planningMasterList list of PlanningMasterViewModel records
     * @param planningDetailsList list of PlanningDetailsModel records
     * @return combined PlanningF5View object
     */
    @Named("createPlanningF5View")
    default PlanningF5View createPlanningF5View(List<PlanningMasterViewModel> planningMasterList,
                                                List<PlanningDetailsModel> planningDetailsList) {
        return PlanningF5View.builder()
                .salemaster(planningMasterList)
                .saledetails(planningDetailsList)
                .build();
    }

    /**
     * Map PlanningF5View to itself (useful for transformations if needed)
     */
    PlanningF5View mapPlanningF5View(PlanningF5View source);

    /**
     * Map list of PlanningMasterViewModel
     */
    List<PlanningMasterViewModel> mapPlanningMasterViewModels(List<PlanningMasterViewModel> source);

    /**
     * Map list of PlanningDetailsModel
     */
    List<PlanningDetailsModel> mapPlanningDetailsModels(List<PlanningDetailsModel> source);
}
