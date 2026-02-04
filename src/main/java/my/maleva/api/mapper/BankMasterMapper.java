package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.BankMaster;
import my.maleva.api.dto.BankMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BankMasterMapper {

    BankMasterDto toDto(BankMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BankMaster toEntity(BankMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BankMasterDto dto, @MappingTarget BankMaster entity);
}
