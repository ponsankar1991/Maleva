package my.maleva.api.mapper;

import my.maleva.api.dto.SupplierDto;
import my.maleva.api.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SupplierMapper - MapStruct mapper for Supplier
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {

    SupplierDto toDto(Supplier entity);

    Supplier toEntity(SupplierDto dto);

    void updateEntityFromDto(SupplierDto dto, @MappingTarget Supplier entity);
}

