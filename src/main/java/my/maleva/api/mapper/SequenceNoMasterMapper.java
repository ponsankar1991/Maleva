package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.SequenceNoMaster;
import my.maleva.api.dto.SequenceNoMasterDto;

/**
 * MapStruct Mapper for SequenceNoMaster Entity to DTO conversion
 * Handles bidirectional mapping between entity and DTO
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SequenceNoMasterMapper {

    /**
     * Convert Entity to DTO
     *
     * @param entity the SequenceNoMaster entity
     * @return the DTO
     */
    SequenceNoMasterDto toDto(SequenceNoMaster entity);

    /**
     * Convert DTO to Entity
     *
     * @param dto the DTO
     * @return the entity
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SequenceNoMaster toEntity(SequenceNoMasterDto dto);

    /**
     * Update entity from DTO
     *
     * @param dto the DTO
     * @param entity the entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(SequenceNoMasterDto dto, @MappingTarget SequenceNoMaster entity);
}

