package my.maleva.api.module.employee.mapper;

import my.maleva.api.module.employee.dto.CapabilityDto;
import my.maleva.api.module.employee.entity.Capability;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CapabilityMapper {
    CapabilityDto toDto(Capability entity);
    Capability toEntity(CapabilityDto dto);
    List<CapabilityDto> toDtoList(List<Capability> entities);
}
