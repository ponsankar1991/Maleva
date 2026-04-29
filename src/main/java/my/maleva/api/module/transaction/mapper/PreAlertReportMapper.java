package my.maleva.api.module.transaction.mapper;

import my.maleva.api.module.transaction.dto.PreAlertReportModel;
import my.maleva.api.module.transaction.dto.PreAlertSearchModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct mapper for PreAlert Report DTO conversions
 * Handles mapping between domain models and DTOs
 */
@Mapper(componentModel = "spring")
public interface PreAlertReportMapper {

    PreAlertReportMapper INSTANCE = Mappers.getMapper(PreAlertReportMapper.class);

    /**
     * Map PreAlertReportModel to another entity if needed
     */
    @Mapping(source = "saleOrderMasterRefId", target = "saleOrderMasterRefId")
    @Mapping(source = "jobNo", target = "jobNo")
    PreAlertReportModel toEntity(PreAlertReportModel model);

    /**
     * Map list of models
     */
    List<PreAlertReportModel> toEntityList(List<PreAlertReportModel> models);

    /**
     * Map PreAlertSearchModel - currently just returns as-is
     * Can be extended for custom search filtering logic
     */
    default PreAlertSearchModel toSearchModel(PreAlertSearchModel model) {
        return model;
    }
}


