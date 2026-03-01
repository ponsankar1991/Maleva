package my.maleva.api.mapper;

import my.maleva.api.dto.SaleOrderForwardingDto;
import my.maleva.api.model.SaleOrderForwarding;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleOrderForwardingMapper - MapStruct mapper for SaleOrderForwarding
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderForwardingMapper {

    SaleOrderForwardingDto toDto(SaleOrderForwarding entity);

    SaleOrderForwarding toEntity(SaleOrderForwardingDto dto);

    void updateEntityFromDto(SaleOrderForwardingDto dto, @MappingTarget SaleOrderForwarding entity);
}

