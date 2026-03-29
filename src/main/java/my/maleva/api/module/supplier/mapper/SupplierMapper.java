package my.maleva.api.module.supplier.mapper;

import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.entity.Supplier;
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

