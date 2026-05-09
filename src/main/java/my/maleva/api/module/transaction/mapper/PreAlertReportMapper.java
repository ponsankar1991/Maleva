package my.maleva.api.module.transaction.mapper;

import my.maleva.api.module.transaction.dto.PreAlertReportModel;
import my.maleva.api.module.transaction.dto.PreAlertSearchModel;
import my.maleva.api.module.transaction.dto.PreAlertMasterDto;
import my.maleva.api.module.transaction.dto.PreAlertDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for PreAlert Report DTO conversions
 * Handles mapping between domain models and DTOs
 * Converts between database entities and API DTOs
 *
 * Usage: Inject via Spring and call mapping methods
 * Example: @Autowired private PreAlertReportMapper mapper;
 */
@Mapper(componentModel = "spring")
public interface PreAlertReportMapper {

    // =====================================================================
    // PreAlertReportModel Mappings
    // =====================================================================

    /**
     * Map PreAlertReportModel - identity mapping (already in correct format)
     * MapStruct handles identity mappings automatically
     */
    PreAlertReportModel toReportModel(PreAlertReportModel model);

    /**
     * Map list of PreAlertReportModel
     */
    List<PreAlertReportModel> toReportModelList(List<PreAlertReportModel> models);

    // =====================================================================
    // PreAlertMasterDto Mappings
    // =====================================================================

    /**
     * Map PreAlertMasterDto with all fields preserved
     * MapStruct handles identity mappings automatically
     */
    PreAlertMasterDto toMasterDto(PreAlertMasterDto master);

    /**
     * Map list of PreAlertMasterDto
     */
    List<PreAlertMasterDto> toMasterDtoList(List<PreAlertMasterDto> masters);

    /**
     * Update existing PreAlertMasterDto with new values
     */
    void updateMasterDto(PreAlertMasterDto source, @MappingTarget PreAlertMasterDto target);

    // =====================================================================
    // PreAlertDto (Detail Rows) Mappings
    // =====================================================================

    /**
     * Map PreAlertDto detail row
     * MapStruct handles identity mappings automatically
     */
    PreAlertDto toDetailDto(PreAlertDto detail);

    /**
     * Map list of PreAlertDto detail rows
     */
    List<PreAlertDto> toDetailDtoList(List<PreAlertDto> details);

    /**
     * Update existing PreAlertDto with new values
     */
    void updateDetailDto(PreAlertDto source, @MappingTarget PreAlertDto target);

    // =====================================================================
    // Search Model Mappings
    // =====================================================================

    /**
     * Map PreAlertSearchModel - identity mapping
     * MapStruct handles identity mappings automatically
     */
    PreAlertSearchModel toSearchModel(PreAlertSearchModel searchModel);
}
