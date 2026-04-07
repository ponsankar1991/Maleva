package my.maleva.api.module.planning.mapper;

import my.maleva.api.module.planning.dto.PlanningDetailsModel;
import my.maleva.api.module.planning.dto.PlanningEditResponseDto;
import my.maleva.api.module.planning.dto.PlanningMasterViewModel;
import my.maleva.api.module.planning.dto.query.PlanningEditMasterRow;
import my.maleva.api.module.planning.dto.query.PlanningSelectMasterRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlanningQueryMapper {

    @Mapping(target = "sdId", constant = "0")
    @Mapping(target = "planningNo", source = "planningNo")
    @Mapping(target = "planningNoDisplay", source = "planningNoDisplay")
    @Mapping(target = "fDate", constant = "")
    @Mapping(target = "tDate", constant = "")
    @Mapping(target = "sFDate", constant = "")
    @Mapping(target = "sTDate", constant = "")
    @Mapping(target = "saleDate", source = "planningDate")
    @Mapping(target = "sSaleDate", source = "planningDate")
    @Mapping(target = "planningDate", source = "planningDate")
    @Mapping(target = "cNumberDisplay", source = "planningNoDisplay")
    @Mapping(target = "remarks", source = "remarks")
    @Mapping(target = "employeeName", source = "employeeName")
    @Mapping(target = "totalOrders", source = "totalOrders")
    @Mapping(target = "active", constant = "1")
    @Mapping(target = "createdDate", constant = "")
    @Mapping(target = "createdBy", constant = "")
    @Mapping(target = "modifiedDate", constant = "")
    @Mapping(target = "modifiedBy", constant = "")
    PlanningMasterViewModel toPlanningMasterViewModel(PlanningSelectMasterRow source);

    @Mapping(target = "fDate", expression = "java(source.getSFDate())")
    @Mapping(target = "tDate", expression = "java(source.getSTDate())")
    @Mapping(target = "sFDate", expression = "java(source.getSFDate())")
    @Mapping(target = "sTDate", expression = "java(source.getSTDate())")
    @Mapping(target = "sSaleDate", expression = "java(source.getSSaleDate())")
    @Mapping(target = "cNumberDisplay", expression = "java(source.getCNumberDisplay())")
    @Mapping(target = "cNumber", expression = "java(source.getCNumber())")
    @Mapping(target = "saleDetails", ignore = true)
    PlanningEditResponseDto toPlanningEditResponse(PlanningEditMasterRow source);

    default PlanningEditResponseDto toPlanningEditResponse(
            PlanningEditMasterRow source,
            List<PlanningDetailsModel> saleDetails
    ) {
        PlanningEditResponseDto response = toPlanningEditResponse(source);
        response.setSaleDetails(saleDetails);
        return response;
    }
}
