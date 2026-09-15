package my.maleva.api.module.supplier.mapper;

import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.entity.Supplier;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SupplierMapper - MapStruct mapper for Supplier
 *
 * <p><b>The builder is disabled on purpose</b>, as in CustomerMapper. With it,
 * MapStruct wrote through Lombok's builder, whose methods are named
 * {@code oEmail(...)}, while the getters {@code getOEmail()} read as the
 * property {@code OEmail}. The names never met, so ten fields — CNumber,
 * CNumberDisplay and the OEmail/OEmail1/OName/OPhone/AEmail/AEmail1/AName/APhone
 * columns — were silently left null. On the supplier screen those are the TIN
 * NO, SST REG NO, MSIC CODE, SERVICE TAX TYPE, BANK NAME and ACCOUNT NUMBER
 * boxes: an edit loaded them blank and the next Save erased them. Setters and
 * getters share one naming rule. Pinned by SupplierMapperTest.
 */
@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {

    SupplierDto toDto(Supplier entity);

    Supplier toEntity(SupplierDto dto);

    void updateEntityFromDto(SupplierDto dto, @MappingTarget Supplier entity);
}
