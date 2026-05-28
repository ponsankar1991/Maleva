package my.maleva.api.module.common.mapper;

import my.maleva.api.module.common.dto.CommonDto;
import org.mapstruct.Mapper;

/**
 * CommonMapper - MapStruct mapper for CommonDto
 * Handles entity ↔ DTO conversion for common models
 */
@Mapper(componentModel = "spring")
public interface CommonMapper {

    /**
     * Convert CommonDto to entity (not needed - DTO is standalone)
     */
    CommonDto toDto(CommonDto entity);

    /**
     * Create new CommonDto from parameters
     */
    default CommonDto createDto(String mobileData, String saleId, Integer comid, Integer id) {
        return CommonDto.builder()
                .mobileData(mobileData)
                .saleId(saleId)
                .comid(comid)
                .id(id)
                .build();
    }
}

