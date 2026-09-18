package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.TruckMasterDto;
import my.maleva.api.module.fleet.entity.TruckMaster;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * TruckMasterMapper - MapStruct mapper for TruckMaster
 *
 * <p><b>The builder is disabled on purpose</b>, as in SupplierMapper and
 * CustomerMapper. With it, MapStruct wrote through Lombok's builder, whose
 * methods are named {@code cNumber(...)}, while the getter {@code getCNumber()}
 * reads as the property {@code CNumber}. The names never met, so
 * {@code CNumber} and {@code CNumberDisplay} were silently dropped in both
 * directions: a new truck reached the database with no CNumberDisplay at all
 * (the column is NOT NULL), and an edit loaded the form with CNumber 0, which
 * made the save hand the truck a brand new number every time. The generated
 * {@code updateEntityFromDto} used setters and mapped them, so only the two
 * builder-based methods were wrong - which is why the columns looked fine on
 * some saves and not others. Setters and getters share one naming rule.
 * Pinned by TruckMasterMapperTest.
 */
@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TruckMasterMapper {

    TruckMasterDto toDto(TruckMaster entity);

    TruckMaster toEntity(TruckMasterDto dto);

    void updateEntityFromDto(TruckMasterDto dto, @MappingTarget TruckMaster entity);
}
