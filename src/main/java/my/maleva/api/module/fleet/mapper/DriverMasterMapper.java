package my.maleva.api.module.fleet.mapper;

import org.mapstruct.*;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.dto.DriverMasterDto;

/**
 * DriverMasterMapper - MapStruct mapper for DriverMaster
 *
 * <p><b>The builder is disabled on purpose</b>, as in TruckMasterMapper,
 * SupplierMapper and CustomerMapper. With it, MapStruct mapped through Lombok's
 * builder, whose method is named {@code cNumber(...)}, while the getter
 * {@code getCNumber()} reads as the property {@code CNumber}. The names never
 * meet and MapStruct drops the field with only a warning, so {@code CNumber} and
 * {@code CNumberDisplay} - the only two fields on this class whose second letter
 * is a capital - were lost by both {@code toDto} and {@code toEntity}. The
 * NOT NULL CNumberDisplay column made every insert fail, and an edit reopened
 * with CNumber 0, which had the next save hand the driver a new number.
 * {@code updateFromDto} uses setters and was never affected, which is what hid
 * it. Pinned by DriverMasterMapperTest.
 */
@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DriverMasterMapper {

    DriverMasterDto toDto(DriverMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DriverMaster toEntity(DriverMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(DriverMasterDto dto, @MappingTarget DriverMaster entity);
}
