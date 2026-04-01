package my.maleva.api.module.saleorder.mapper;

import my.maleva.api.module.saleorder.dto.ForwardingDetailDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderForwardingDto;
import my.maleva.api.module.saleorder.entity.SaleOrderForwarding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleOrderForwardingMapper - MapStruct mapper for SaleOrderForwarding
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderForwardingMapper {

    SaleOrderForwardingDto toDto(SaleOrderForwarding entity);

    SaleOrderForwarding toEntity(SaleOrderForwardingDto dto);

    @Mapping(target = "sealByRefId", ignore = true)
    @Mapping(target = "breakSealByRefId", ignore = true)
    SaleOrderForwarding toEntity(ForwardingDetailDTO dto);

    void updateEntityFromDto(SaleOrderForwardingDto dto, @MappingTarget SaleOrderForwarding entity);
}
