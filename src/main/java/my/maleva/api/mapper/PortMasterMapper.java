package my.maleva.api.mapper;

import my.maleva.api.dto.PortMasterDto;
import my.maleva.api.model.PortMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PortMasterMapper {

    /**
     * Map PortMaster entity to PortMasterDto
     */
    PortMasterDto toDto(PortMaster entity);

    /**
     * Map PortMasterDto to PortMaster entity
     */
    PortMaster toEntity(PortMasterDto dto);

    /**
     * Update PortMaster entity from PortMasterDto
     */
    void updateFromDto(PortMasterDto dto, @MappingTarget PortMaster entity);
}

